package com.totodon.molitan.gae.utils;

import com.google.appengine.api.datastore.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

@SuppressWarnings("unchecked")
public class Ranker
{

	private static final Logger log = Logger.getLogger(Ranker.class.getName());

	// Class variables
	private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	private Key rootkey;
	private ArrayList<Long> score_range = new ArrayList<Long>();
	private long branching_factor;
 
	// 0-argument constructor for Java
	public Ranker()
	{
 
	}
 
	/**
	 * Pulls a ranker out of the datastore, given the key of the root node.
	 * 
	 * @throws EntityNotFoundException
	 * 
	 * @param rootkey
	 *            The datastore key of the ranker.
	 */
	// @SuppressWarnings("unchecked")
	public Ranker(String rootkey) throws EntityNotFoundException
	{
		Entity root;
		root = datastore.get(KeyFactory.stringToKey(rootkey));
		// Initialize some class variables
		this.rootkey = KeyFactory.stringToKey(rootkey);
		// Need to convert arrayList of Longs to Integers
		this.score_range = (ArrayList<Long>) root.getProperty("score_range");
		this.branching_factor = (Long) root.getProperty("branching_factor");
		// Sanity checking
		assert this.score_range.size() > 1;
		assert this.score_range.size() % 2 == 0;
		for (int i = 0; i < this.score_range.size(); i = i + 2)
			assert this.score_range.get(i + 1) > this.score_range.get(i);
		assert this.branching_factor > 1;
	}
 
	/**
	 * Constructs a new Ranker and returns it.
	 * 
	 * @param score_range
	 *            A list showing the range of valid scores, in the form:
	 *            [most_significant_score_min, most_significant_score_max,
	 *            less_significant_score_min, less_significant_score_max, ...]
	 *            Ranges are [inclusive, exclusive)
	 * @param branching_factor
	 *            The branching factor of the tree. The number of datastore Gets
	 *            is Theta(1/log(branching_factor)), and the amount of data
	 *            returned by each Get is Theta(branching_factor).
	 * @return
	 * @throws EntityNotFoundException
	 */
	public Ranker Create(String rankerName, ArrayList<Integer> score_range, int branching_factor) throws EntityNotFoundException
	{
		Entity root = new Entity(rankerName);
		root.setProperty("score_range", score_range);
		root.setProperty("branching_factor", branching_factor);
		datastore.put(root);
		return new Ranker(KeyFactory.keyToString(root.getKey()));
	}
 
	public String getKeyString()
	{
		return KeyFactory.keyToString(this.rootkey);
	}
 
 
	/**
	 * Finds the nodes along the path from the root to a certain score.
	 * 
	 * Nodes are numbered row-by-row: the root is 0, its children are in the
	 * range [1, self.branching_factor + 1), its grandchildren are in the range
	 * [self.branching_factor + 1, self.branching_factor**2 +
	 * self.branching_factor + 1), etc.
	 * 
	 * Score ranges are lists of the form: [min_0, max_0, min_1, max_1, ...] A
	 * node representing a score range will be divided up by the first index
	 * where max_i != min_i + 1 (score ranges are [inclusive, exclusive)).
	 * 
	 * Child x (0-indexed) of a node [a,b) will get the range:
	 * [a+x*(b-a)/branching_factor, a+(x+1)*(b-a)/branching_factor); Thus not
	 * all nodes will have nonzero ranges. Nodes with zero range will never be
	 * visited, but they and their descendants will be counted in the node
	 * numbering scheme, so row x still has self.branching_factor**x nodes.
	 * 
	 * @param score
	 *            The score we're finding the path for.
	 * @return A sorted list of (node_id, child) tuples, indicating that node_id
	 *         is the node id of a node on the path, and child is which child of
	 *         that node is next. Note that the lowest child node (which would
	 *         be a leaf node) does not actually exist, since all its relevant
	 *         information (number of times that score was inserted) is stored
	 *         in its parent.
	 */
	public ArrayList<NodeID_Child_Tuple> FindNodeIDs(Score_Tuple score) throws Exception
	{
		ArrayList<NodeID_Child_Tuple> nodes = new ArrayList<NodeID_Child_Tuple>();
		long node = 0;
 
		ArrayList<Long> cur_range = new ArrayList<Long>();
		for (Iterator<Long> iter = this.score_range.iterator(); iter.hasNext();)
		{
			cur_range.add(iter.next());
		}
 
		Child_ChildScoreRange_Tuple which_child;
		long child;
 
		// The current range of scores. This will be narrowed as we move down
		// the tree; 'index' keeps track of the score type we're currently
		// changing.
		for (int i = 0; i < cur_range.size(); i += 2)
		{
			while (cur_range.get(i + 1) - cur_range.get(i) > 1)
			{
				// Subdivide cur_range[index]..cur_range[index + 1]
				which_child = WhichChild(cur_range.get(i), cur_range.get(i + 1), score.getByIndex(i / 2), this.branching_factor);
				child = which_child.getChild();
				cur_range.set(i, which_child.getChildScoreRange().get(0));
				cur_range.set(i + 1, which_child.getChildScoreRange().get(1));
				assert 0 <= child;
				assert child < this.branching_factor;
				nodes.add(new NodeID_Child_Tuple(node, child));
				node = ChildNodeId(node, child);
			}
		}
 
		return nodes;
	}
 
	/**
	 * Determines which child of the range [low, high) 'want' belongs to.
	 * 
	 * @param loow
	 *            An int, the low end of the range.
	 * @param high
	 *            An int, the high end of the range.
	 * @param want
	 *            An int, the score we're trying to determine a child for.
	 * @param branching_factor
	 *            The branching factor of the tree being used.
	 * 
	 * @return A tuple, (child, [child's score range]). Note that in general a
	 *         score has multiple sub-scores, written in order of decreasing
	 *         significance; this function divides up a single sub-score.
	 * @throws Exception
	 */
	private Child_ChildScoreRange_Tuple WhichChild(Long low, Long high, int want, Long branching_factor) throws Exception
	{
		long x;
 
		assert low <= want;
		assert want < high;
 
		/*
		 * Need to find x such that (using integer division): x
		 * *(high-low)/branching_factor <= want - low <
		 * (x+1)*(high-low)/branching_factor Which is the least x such that
		 * (using integer division): want - low <
		 * (x+1)*(high-low)/branching_factor Which is the ceiling of x such that
		 * (using floating point division): want - low + 1 ==
		 * (x+1)*(high-low)/branching_factor x = -1 + math.ceil((want-low+1) *
		 * branching_factor / (high - low)) We get ceil by adding high - low - 1
		 * to the numerator.
		 */
 
		x = -1 + (((want - low + 1) * branching_factor + high - low - 1) / (high - low));
 
		assert (x * (high - low) / branching_factor <= want - low);
		assert (want - low < (x + 1) * (high - low) / branching_factor);
 
		ArrayList<Long> score_range = new ArrayList<Long>();
		score_range.add(low);
		score_range.add(high);
		return new Child_ChildScoreRange_Tuple(x, ChildScoreRange(score_range, x, branching_factor));
	}
 
	/**
	 * Calculates the score_range for a node's child.
	 * 
	 * @param score_range
	 *            A score range [min0, max0, min1, max1, ...]
	 * @param x
	 *            Which child of the node with score range score_range we're
	 *            calculating the score range of.
	 * @param branching_factor2
	 *            The branching factor of the tree in question.
	 * @return A score range [min0', max0', min1', max1', ...] for that child.
	 * @throws Exception
	 */
	private ArrayList<Long> ChildScoreRange(ArrayList<Long> score_range, long child, long branching_factor) throws Exception
	{
		ArrayList<Long> child_score_range = new ArrayList<Long>();
		long low;
		long high;
 
		for (int i = 1; i < score_range.size(); i = i + 2)
		{
			if (score_range.get(i) > score_range.get(i - 1) + 1)
			{
				// child_score_range = score_range.clone();
				for (Iterator<Long> iter = score_range.iterator(); iter.hasNext();)
					child_score_range.add(iter.next());
				low = score_range.get(i - 1);
				high = score_range.get(i);
				child_score_range.set(i - 1, (low + child * (high - low) / branching_factor));
				child_score_range.set(i, (low + (child + 1) * (high - low) / branching_factor));
				return child_score_range;
			}			
		}
		throw new Exception("Node with score range " + score_range + " has no children.");
	}
 
	/**
	 * Calculates the node id for a known node id's child.
	 * 
	 * @param node
	 *            The parent node's node_id
	 * @param child
	 *            Which child of the parent node we're finding the id for
	 * @return The node_id for the child'th child of node_id.
	 */
	private long ChildNodeId(long node, long child)
	{
		return node * this.branching_factor + 1 + child;
	}
 
	public Map<Long, Entity> GetMultipleNodes(ArrayList<Long> node_ids)
	{
		ArrayList<Key> keys = new ArrayList<Key>();
		Map<Key, Entity> nodes;
 
		Map<Long, Entity> dict = new HashMap<Long, Entity>();
 
		if (node_ids.size() == 0)
			return new HashMap<Long, Entity>();
 
		// Remove dupes, preserve insertion order
		Set<Long> set = new LinkedHashSet<Long>(node_ids);
		for (Iterator<Long> iter = set.iterator(); iter.hasNext();)
			keys.add(KeyFromNodeId(iter.next()));
 
		nodes = datastore.get(keys);
 
		int i = 0;
		for (Iterator<Long> iter = set.iterator(); iter.hasNext();)
		{
			dict.put(iter.next(), nodes.get(keys.get(i)));
			i++;
		}
		return dict;
	}
 
	// # Although, this method is currently not needed, we'll keep this since we
	// might need it and some point and it's an interesting relationship
	/**
	 * 
	 * @param node_id
	 * @return The node id of the parameter node id's parent. Returns -1 if the
	 *         parameter is 0.
	 */
	@SuppressWarnings("unused")
	private long ParentNode(int node_id)
	{
		if (node_id == 0)
			return -1;
		return (node_id - 1) / this.branching_factor;
	}
 
	/**
	 * Creates a (named) key for the node with a given id.
	 * 
	 * The key will have the ranker as a parent element to guarantee uniqueness
	 * (in the presence of multiple rankers) and to put all nodess in a single
	 * entity group.
	 * 
	 * @param node_id
	 *            The node's id as an integer.
	 * 
	 * @return A (named) key for the node with the id 'node_id'.
	 */
	private Key KeyFromNodeId(long node_id)
	{
		String name = "node_" + Long.toString(node_id);
		return KeyFactory.createKey(this.rootkey, "ranker_node", name);
	}
 
	/**
	 * Returns a (named) key for a ranker_score entity.
	 * 
	 * @param name
	 *            Name of the score to create a key for.
	 * @return A (named) key for the entity storing the score of 'name'.
	 */
	private Key KeyForScore(String name)
	{
		return KeyFactory.createKey(this.rootkey, "ranker_score", name);
	}
 
	/**
	 * """Changes child counts for given nodes. This method will create nodes as
	 * needed.
	 * 
	 * @param node_ids_to_deltas
	 *            A dict of (node_key, child) tuples to deltas
	 * @param score_entities
	 *            Additional score entities to persist as part of this
	 *            transaction
	 * @param score_entities_to_delete
	 *            Additional score entities to delete as part of this
	 *            transaction
	 */
	private void Increment(HashMap<Key_Child_Tuple, Integer> node_ids_to_deltas, ArrayList<Entity> score_entities,
			ArrayList<Entity> score_entities_to_delete)
	{
		ArrayList<Key> keys = new ArrayList<Key>();
		Key_Child_Tuple tuple;
		Map<Key, Entity> nodes;
		Map<Key, Entity> node_dict = new HashMap<Key, Entity>();
		Entity node;
 
		for (Iterator<Key_Child_Tuple> iter = node_ids_to_deltas.keySet().iterator(); iter.hasNext();)
		{
			tuple = iter.next();
			if (node_ids_to_deltas.get(tuple) != 0)
			{
				keys.add((Key) tuple.getKey());
			}
		}
 
		if (keys.isEmpty())
			return;
 
		nodes = datastore.get(keys);
 
		for (int i = 0; i < keys.size(); i++)
		{
			if (!nodes.containsKey(keys.get(i)))
			{
				node = new Entity("ranker_node", keys.get(i).getName(), this.rootkey);
 
				ArrayList<Long> branchingList = new ArrayList<Long>();
				for (int j = 0; j < this.branching_factor; j++)
					branchingList.add((long) 0);
				node.setProperty("child_counts", branchingList);
			}
 
			else
			{
				node = nodes.get(keys.get(i));
			}
 
			node_dict.put(keys.get(i), node);
		}
 
		for (Iterator<Key_Child_Tuple> iter = node_ids_to_deltas.keySet().iterator(); iter.hasNext();)
		{
			tuple = iter.next();
			int amount = node_ids_to_deltas.get(tuple);
			if (amount != 0)
			{
				ArrayList<Long> child_counts = new ArrayList<Long>();
				node = node_dict.get(tuple.getKey());
 
				child_counts = ((ArrayList<Long>) node.getProperty("child_counts"));
				Long a = child_counts.get((int) tuple.getChild());
				a += amount;
				child_counts.set((int) tuple.getChild(), (long) a);
				assert ((ArrayList<Long>) node.getProperty("child_counts")).get((int) tuple.getChild()) >= 0;
			}
		}
 
		ArrayList<Entity> entities = new ArrayList<Entity>();
		for (Iterator<Entity> entityiter = node_dict.values().iterator(); entityiter.hasNext();)
			entities.add(entityiter.next());
 
		entities.addAll(score_entities);
		datastore.put(entities);
 
		ArrayList<Key> keyEntitiesToDelete = new ArrayList<Key>();
		for (int i = 0; i < score_entities_to_delete.size(); i++)
			keyEntitiesToDelete.add(score_entities_to_delete.get(i).getKey());
 
		if (!score_entities_to_delete.isEmpty())
			datastore.delete(keyEntitiesToDelete);
	}
 
	/**
	 * Sets a single score. This is equivalent to calling 'SetScores({name:
	 * score})'
	 * 
	 * @param name
	 *            the name of the score as a string
	 * @param score
	 *            the score to set name to
	 * @return
	 * @throws Exception
	 */
	public void SetScore(String name, Score_Tuple score) throws Exception
	{
		HashMap<String, Score_Tuple> map = new HashMap<String, Score_Tuple>();
		map.put(name, score);
		SetScores(map);
	}
 
	// Tuple<HashMap<Tuple<String, Integer>, Integer>, ArrayList<Entity>,
	// ArrayList<Entity>>
 
	/**
	 * Changes multiple scores atomically. Sets the scores of the named entities
	 * in scores to new values. For named entities that have not been registered
	 * with a score before, a new score is created. For named entities that
	 * already had a score, the score is changed to reflect the new score. If a
	 * score is None, the named entity's score will be removed from the ranker.
	 * 
	 * @param scores
	 *            A dict mapping entity names (strings) to scores (integer
	 *            lists)
	 * @throws Exception
	 */
	public void SetScores(HashMap<String, Score_Tuple> scores) throws Exception
	{
		ScoreDeltas_ScoreEnts_ScoreEntsDel_Tuple tuple;
		Map<Score_Tuple, Integer> score_deltas;
		ArrayList<Entity> score_ents;
		ArrayList<Entity> score_ents_del;
		HashMap<Key_Child_Tuple, Integer> node_ids_to_deltas;
 
		tuple = ComputeScoreDeltas(scores);
 
		score_deltas = (Map<Score_Tuple, Integer>) tuple.getScore_deltas();
		score_ents = (ArrayList<Entity>) tuple.getScore_ents();
		score_ents_del = (ArrayList<Entity>) tuple.getScore_ents_del();
 
		node_ids_to_deltas = ComputeNodeModifications(score_deltas);
 
		Increment(node_ids_to_deltas, score_ents, score_ents_del);
	}
 
	/**
	 * Compute which scores have to be incremented and decremented.
	 * 
	 * @param scores
	 *            A dict mapping entity names to scores
	 * @return ScoreDeltas_ScoreEnts_ScoreEntsDel_Tuple 'score_deltas' is a
	 *         dict, mapping scores (represented as tuples) to integers.
	 *         'score_deltas[s]' represents how many times the score 's' has to
	 *         be incremented (or decremented).
	 * 
	 *         'score_entities' is a list of 'ranker_score' entities that have
	 *         to be updated in the same transaction as modifying the ranker
	 *         nodes. The entities already contain the updated score.
	 * 
	 *         Similarly, 'score_entities_to_delete' is a list of entities that
	 *         have to be deleted in the same transaction as modifying the
	 *         ranker nodes.
	 */
	private ScoreDeltas_ScoreEnts_ScoreEntsDel_Tuple ComputeScoreDeltas(HashMap<String, Score_Tuple> scores)
	{
		ArrayList<Key> score_keys = new ArrayList<Key>();
		for (Iterator<String> scoreiter = scores.keySet().iterator(); scoreiter.hasNext();)
			score_keys.add(KeyForScore(scoreiter.next()));
 
		Map<String, Entity> old_scores = new HashMap<String, Entity>();
		for (Iterator<Entity> oldScoresFromDatastoreIter = datastore.get(score_keys).values().iterator(); oldScoresFromDatastoreIter.hasNext();)
		{
			Entity old_score = oldScoresFromDatastoreIter.next();
			old_scores.put(old_score.getKey().getName(), old_score);
		}
 
		HashMap<Score_Tuple, Integer> score_deltas = new HashMap<Score_Tuple, Integer>();
		// Score entities to update
		ArrayList<Entity> score_ents = new ArrayList<Entity>();
		ArrayList<Entity> score_ents_del = new ArrayList<Entity>();
 
		for (Iterator<Entry<String, Score_Tuple>> scoreiter = scores.entrySet().iterator(); scoreiter.hasNext();)
		{
			Entry<String, Score_Tuple> scoreEntry = scoreiter.next();
			String score_name = scoreEntry.getKey();
			Score_Tuple score_value = scoreEntry.getValue();
 
			Entity score_ent;
			Score_Tuple old_score_key;
			Score_Tuple score_key;
 
			if (old_scores.containsKey(score_name))
			{
				score_ent = old_scores.get(score_name);
 
				if (new Score_Tuple((String) score_ent.getProperty("value")).equals(score_value))
				{
					continue; // No change in score => nothing to do
				}
 
				old_score_key = new Score_Tuple(((String) score_ent.getProperty("value")));
 
				if (!score_deltas.containsKey(old_score_key))
					score_deltas.put(old_score_key, 0);
 
				score_deltas.put(old_score_key, score_deltas.get(old_score_key) - 1);
 
			}
 
			else
			{
				score_ent = new Entity("ranker_score", score_name, this.rootkey);
			}
 
			if (score_value != null) // WARNING: line 453????
			{
				score_key = new Score_Tuple(score_value.getScore(), score_value.getScore_time());
 
				if (!score_deltas.containsKey(score_key))
					score_deltas.put(score_key, 0);
 
				score_deltas.put(score_key, ((Integer) score_deltas.get(score_key)) + 1);
				score_ent.setProperty("value", score_value.toString());
				score_ents.add(score_ent);
			}
 
			else
			{
				// Do we have to delete an old score entity?
				if (old_scores.containsKey(score_name))
				{
					score_ents_del.add(old_scores.get(score_name));
				}
			}
 
		}
 
		return new ScoreDeltas_ScoreEnts_ScoreEntsDel_Tuple(score_deltas, score_ents, score_ents_del);
	}
 
	/**
	 * Computes modifications to ranker nodes. Given score deltas, computes
	 * which nodes need to be modified and by how much their child count has to
	 * be incremented / decremented.
	 * 
	 * @param score_deltas
	 *            A dict of scores to integers, as returned by
	 *            _ComputeScoreDeltas.
	 * @return A dict of nodes (represented as node_key, child tuples) to
	 *         integers. 'result[(node_key, i)]' represents the amount that
	 *         needs to be added to the i-th child of node node_key.
	 * @throws Exception
	 */
	private HashMap<Key_Child_Tuple, Integer> ComputeNodeModifications(Map<Score_Tuple, Integer> score_deltas) throws Exception
	{
		HashMap<Key_Child_Tuple, Integer> node_to_deltas = new HashMap<Key_Child_Tuple, Integer>();
 
		for (Iterator<Entry<Score_Tuple, Integer>> iter = score_deltas.entrySet().iterator(); iter.hasNext();)
		{
			Entry<Score_Tuple, Integer> entry = iter.next();
			Score_Tuple score = entry.getKey();
			int delta = entry.getValue();
 
			ArrayList<NodeID_Child_Tuple> nodeIDs = FindNodeIDs(score);
			for (Iterator<NodeID_Child_Tuple> iter2 = nodeIDs.iterator(); iter2.hasNext();)
			{
				NodeID_Child_Tuple nodeTuple = iter2.next();
				long node_id = nodeTuple.getNode_id();
				long child = nodeTuple.getChild();
				Key_Child_Tuple node = new Key_Child_Tuple(KeyFromNodeId(node_id), child);
 
				try
				{
					node_to_deltas.put(node, node_to_deltas.get(node) + delta);
				} catch (NullPointerException e)
				{
					node_to_deltas.put(node, delta);
				}
			}
		}
 
		return node_to_deltas;
	}
 
	/**
	 * Utility function. Finds the rank of a score.
	 * 
	 * @param node_ids_with_children
	 *            A list of node ids down to that score, paired with which child
	 *            links to follow.
	 * @param nodes_dict
	 *            A dict mapping node id to node entity.
	 * @return The score's rank.
	 */
	private long FindRank(ArrayList<NodeID_Child_Tuple> node_ids_with_children, Map<Long, Entity> nodes_dict)
	{
		long tot = 0; // Counts the number of higher scores
 
		for (Iterator<NodeID_Child_Tuple> iter = node_ids_with_children.iterator(); iter.hasNext();)
		{
			NodeID_Child_Tuple node_id_with_child = iter.next();
			long node_id = node_id_with_child.getNode_id();
			long child = node_id_with_child.getChild();
 
			if (nodes_dict.containsKey(node_id))
			{
				Entity node = nodes_dict.get(node_id);
				for (int i = (int) (child + 1); i < this.branching_factor; i++)
				{
					// tot += ((int[]) node.getProperty("child_counts"))[i];
					tot += ((ArrayList<Long>) node.getProperty("child_counts")).get(i);
				}
			}
 
			else
			{
				// If the node isn't in the dict, the node simply doesn't exist.
				// We are probably finding the rank for a score that doesn't
				// appear in the ranker, but that's perfectly fine.
				break;
			}
		}
		return tot;
	}
 
	/**
	 * Finds the 0-based rank of a particular score; more precisely, returns the
	 * number of strictly higher scores stored.
	 * 
	 * @param score
	 *            The score whose rank we wish to find.
	 * @return The number of tracked scores that are higher. Does not check
	 *         whether anyone actually has the requested score.
	 * @throws Exception
	 */
	public ArrayList<Long> FindRank(Score_Tuple score) throws Exception
	{
		ArrayList<Score_Tuple> scoreList = new ArrayList<Score_Tuple>();
		scoreList.add(score);
		return FindRanks(scoreList);
	}
 
	/**
	 * Finds the 0-based ranks of a number of particular scores. Like FindRank,
	 * but more efficient for multiple scores.
	 * 
	 * @param scores
	 *            A list of scores.
	 * @return A list of ranks.
	 * @throws Exception
	 */
	public ArrayList<Long> FindRanks(ArrayList<Score_Tuple> scores) throws Exception
	{
		ArrayList<ArrayList<NodeID_Child_Tuple>> node_ids_with_children_list = new ArrayList<ArrayList<NodeID_Child_Tuple>>();
		ArrayList<Long> node_ids = new ArrayList<Long>();
		Map<Long, Entity> nodes_dict;
		ArrayList<Long> ranks = new ArrayList<Long>();
 
		// Find the nodes we'll need to query to find information about these
		// scores:
		for (Iterator<Score_Tuple> iter = scores.iterator(); iter.hasNext();)
			node_ids_with_children_list.add(FindNodeIDs(iter.next()));
 
		for (Iterator<ArrayList<NodeID_Child_Tuple>> iter = node_ids_with_children_list.iterator(); iter.hasNext();)
		{
			ArrayList<NodeID_Child_Tuple> node_ids_with_childern = new ArrayList<NodeID_Child_Tuple>();
			node_ids_with_childern = iter.next();
			for (int i = 0; i < node_ids_with_childern.size(); i++)
			{
				node_ids.add(node_ids_with_childern.get(i).getNode_id());
			}
		}
 
		// Query the needed nodes
		nodes_dict = GetMultipleNodes(node_ids);
 
		// # Call __FindRank, which does the math, for each score:
		for (Iterator<ArrayList<NodeID_Child_Tuple>> iter = node_ids_with_children_list.iterator(); iter.hasNext();)
		{
			ArrayList<NodeID_Child_Tuple> node_ids_with_childern = new ArrayList<NodeID_Child_Tuple>();
			node_ids_with_childern = iter.next();
			ranks.add(FindRank(node_ids_with_childern, nodes_dict));
		}
		return ranks;
	}
 
	/**
	 * To be run in a transaction. Finds the score ranked 'rank' in the subtree
	 * defined by node 'nodekey.'
	 * 
	 * @param node_id
	 *            The id of the node whose subtree we wish to find the score of
	 *            rank 'rank' in.
	 * @param rank
	 *            The rank (within this subtree) of the score we wish to find.
	 * @param score_range
	 *            The score range for this particular node, as a list. Derivable
	 *            from the node's node_id, but included for convenience.
	 * @param approximateDo
	 *            we have to return an approximate result, or an exact one? See
	 *            the docstrings for FindScore and FindScoreApproximate.
	 * @returnA tuple, (score, rank_of_tie), indicating the score's rank within
	 *          node_id's subtree. The way it indicates rank is defined in the
	 *          dosctrings of FindScore and FindScoreApproximate, depending on
	 *          the value of 'approximate'.
	 * @throws EntityNotFoundException
	 * @throws Exception
	 */
	private Score_RankOfTie_Tuple FindScore(long node_id, int rank, ArrayList<Long> score_range, boolean approximate) throws EntityNotFoundException,
			Exception
	{
		// # If we're approximating and thus allowed to do so, early-out if we
		// just need to return the highest available score.
		if (approximate && rank == 0)
		{
			ArrayList<Long> arr = new ArrayList<Long>();
			for (int i = 1; i < score_range.size(); i += 2)
				arr.add(score_range.get(i) - 1);
 
			return new Score_RankOfTie_Tuple(arr, 0);
		}
 
		// Find the current node.
		Entity node = datastore.get(KeyFromNodeId(node_id));
		ArrayList<Long> child_counts = (ArrayList<Long>) node.getProperty("child_counts");
		int initial_rank = rank;
		for (int i = ((int) this.branching_factor - 1); i > -1; i--)
		{
			// If this child has enough scores that rank 'rank' is in there,
			// recurse.
			ArrayList<Long> child_score_range = ChildScoreRange(score_range, i, this.branching_factor);
			if (rank - child_counts.get(i) < 0)
			{
				child_score_range = ChildScoreRange(score_range, i, this.branching_factor);
 
				if (IsSingletonRange(child_score_range))
				{
					// # Base case; child_score_range refers to a single score.
					// We don't store leaf nodes so we can return right here.
					ArrayList<Long> arr = new ArrayList<Long>();
					for (int j = 0; j < child_score_range.size(); j += 2)
						arr.add(child_score_range.get(j));
 
					return new Score_RankOfTie_Tuple(arr, initial_rank - rank);
				}
 
				// Not a base case. Keep descending into children.
				Score_RankOfTie_Tuple ans = FindScore(ChildNodeId(node_id, i), rank, child_score_range, approximate);
 
				// # Note the 'initial_rank - rank': we've asked the child for a
				// score of some rank among *its* children, so we have to add
				// back in the scores discarded on the way to that child.
				return new Score_RankOfTie_Tuple(ans.getScore(), ans.getRank_of_tie() - rank);
			} else
			{
				rank -= child_counts.get(i);
			}
		}
 
		log.fine("FindScore(int node_id, int rank, ArrayList<Integer> score_range, int approximate) returns null!");
		return null;
	}
 
	/**
	 * Returns whether a range contains exactly one score.
	 * 
	 * @param child_score_range
	 * @return
	 */
	private boolean IsSingletonRange(ArrayList<Long> child_score_range)
	{
		boolean var = true;
		for (int i = 0; i < child_score_range.size(); i += 2)
		{
			if (child_score_range.get(i) + 1 != child_score_range.get(i + 1))				
			{
				var = false;
			}
		}
		return var;
 
	}
 
	/**
	 * Finds the score ranked at 'rank'.
	 * 
	 * @param rank
	 *            The rank of the score we wish to find.
	 * @return A tuple, (score, rank_of_tie). 'score' is the score ranked at
	 *         'rank', 'rank_of_tie' is the rank of that score (which may be
	 *         different from 'rank' in the case of ties). e.g. if there are two
	 *         scores tied at 5th and rank == 6, returns (score, 5).
	 * @throws EntityNotFoundException
	 * @throws Exception
	 */
	public Score_RankOfTie_Tuple FindScore(int rank) throws EntityNotFoundException, Exception
	{
		return FindScore(0, rank, this.score_range, false);
 
	}
 
	/**
	 * Finds a score that >= the score ranked at 'rank'. This method could be
	 * preferred to FindScore because it is more efficient. For example, if the
	 * objective is to find the top 50 scores of rank X or less, and those
	 * scores are stored in entities called scoreboard_row: score, rank =
	 * myrank.FindScoreApproximate(X) query = datastore.Query('scoreboard_row')
	 * query['score <='] = score result = query.Get(50 + X - rank)[X-rank:]) #
	 * Takes care of ties.
	 * 
	 * @param rank
	 *            The rank of the score we wish to find.
	 * @return A tuple, (score, rank_of_tie). If there is a tie at rank
	 *         'rank-1': rank's score <= score < rank-1's score, rank_of_tie ==
	 *         rank else: score == rank's score, rank_of_tie == the tied rank of
	 *         everyone in the tie. e.g. if two scores are tied at 5th and rank
	 *         == 6, returns (score, 5).
	 * @throws EntityNotFoundException
	 * @throws Exception
	 */
	public Score_RankOfTie_Tuple FindScoreApproximate(int rank) throws EntityNotFoundException, Exception
	{
		return FindScore(0, rank, this.score_range, true);
	}
 
	/**
	 * Returns the total number of ranked scores.
	 * 
	 * @return The total number of ranked scores.
	 * @throws EntityNotFoundException
	 */
	public int TotalRankedScores() throws EntityNotFoundException
	{
		int sum = 0;
		Entity root = datastore.get(KeyFromNodeId(0));
		ArrayList<Long> root_counts = (ArrayList<Long>) root.getProperty("child_counts");
		for (int i = 0; i < root_counts.size(); i++)
			sum += root_counts.get(i);
 
		return sum;
	}
 
	// Custom Tuple Data Structures
	protected final class NodeID_Child_Tuple
	{
		private long node_id;
		private long child;
 
		NodeID_Child_Tuple(long node, long child)
		{
			this.node_id = node;
			this.child = child;
		}
 
		public long getNode_id()
		{
			return node_id;
		}
 
		public void setNode_id(long node_id)
		{
			this.node_id = node_id;
		}
 
		public long getChild()
		{
			return child;
		}
 
		public void setChild(long child)
		{
			this.child = child;
		}
 
	}
 
	protected final class Child_ChildScoreRange_Tuple
	{
		private long child;
		private ArrayList<Long> childScoreRange;
 
		Child_ChildScoreRange_Tuple(long child, ArrayList<Long> arrayList)
		{
			this.child = child;
			this.childScoreRange = arrayList;
		}
 
		public long getChild()
		{
			return child;
		}
 
		public void setChild(long child)
		{
			this.child = child;
		}
 
		public ArrayList<Long> getChildScoreRange()
		{
			return childScoreRange;
		}
 
		public void setChildScoreRange(ArrayList<Long> childScoreRange)
		{
			this.childScoreRange = childScoreRange;
		}
	}
 
	public final static class Score_Tuple
	{
		private int score;
		private int score_time;
 
		private Score_Tuple(String s)
		{
			String[] arr = s.split(",");
			this.score = Integer.parseInt(arr[0]);
			this.score_time = Integer.parseInt(arr[1]);
		}
 
		public Score_Tuple(int score, int score_time)
		{
			this.score = score;
			this.score_time = score_time;
		}
 
		public int getByIndex(int i)
		{
			if (i == 0)
			{
				return score;
			}
 
			else if (i == 1)
			{
				return score_time;
			}
 
			else
			{
				log.fine("Score_Tuple: getByIndex() exception");
				return -1;
			}
		}
 
		public int getScore()
		{
			return score;
		}
 
		public void setScore(int score)
		{
			this.score = score;
		}
 
		public int getScore_time()
		{
			return score_time;
		}
 
		public void setScore_time(int score_time)
		{
			this.score_time = score_time;
		}
 
		@Override
		public String toString()
		{
			return Integer.toString(score) + "," + Integer.toString(score_time);
		}
	}
 
	protected final class Key_Child_Tuple
	{
		private Key key;
		private long child;
 
		Key_Child_Tuple(Key key, long child)
		{
			this.key = key;
			this.child = child;
		}
 
		public Key getKey()
		{
			return key;
		}
 
		public void setKey(Key key)
		{
			this.key = key;
		}
 
		public long getChild()
		{
			return child;
		}
 
		public void setChild(long child)
		{
			this.child = child;
		}
	}
 
	protected final class ScoreDeltas_ScoreEnts_ScoreEntsDel_Tuple
	{
		private HashMap<Score_Tuple, Integer> score_deltas;
		private ArrayList<Entity> score_ents;
		private ArrayList<Entity> score_ents_del;
 
		ScoreDeltas_ScoreEnts_ScoreEntsDel_Tuple(HashMap<Score_Tuple, Integer> score_deltas, ArrayList<Entity> score_ents,
				ArrayList<Entity> score_ents_del)
		{
			this.score_deltas = score_deltas;
			this.score_ents = score_ents;
			this.score_ents_del = score_ents_del;
		}
 
		public HashMap<Score_Tuple, Integer> getScore_deltas()
		{
			return score_deltas;
		}
 
		public void setScore_deltas(HashMap<Score_Tuple, Integer> score_deltas)
		{
			this.score_deltas = score_deltas;
		}
 
		public ArrayList<Entity> getScore_ents()
		{
			return score_ents;
		}
 
		public void setScore_ents(ArrayList<Entity> score_ents)
		{
			this.score_ents = score_ents;
		}
 
		public ArrayList<Entity> getScore_ents_del()
		{
			return score_ents_del;
		}
 
		public void setScore_ents_del(ArrayList<Entity> score_ents_del)
		{
			this.score_ents_del = score_ents_del;
		}
	}
 
	protected final class Score_RankOfTie_Tuple
	{
		ArrayList<Long> score;
		int rank_of_tie;
 
		Score_RankOfTie_Tuple(ArrayList<Long> arr, int rank_of_tie)
		{
			this.score = arr;
			this.rank_of_tie = rank_of_tie;
		}
 
		public ArrayList<Long> getScore()
		{
			return score;
		}
 
		public void setScore(ArrayList<Long> score)
		{
			this.score = score;
		}
 
		public int getRank_of_tie()
		{
			return rank_of_tie;
		}
 
		public void setRank_of_tie(int rank_of_tie)
		{
			this.rank_of_tie = rank_of_tie;
		}
	}
}
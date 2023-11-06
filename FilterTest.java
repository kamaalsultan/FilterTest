package com.almworks.structure.cloud.commons.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.List;
import java.util.ArrayList;

/**
 * <p>A <tt>Hierarchy</tt> stores an arbitrary <em>forest</em> (an ordered collection of ordered trees)
 * as an array indexed by DFS-order traversal.
 * A node is represented by a unique ID.
 * Parent-child relationships are identified by the position in the array and the associated depth.
 * Tree root has depth 0, immediate children have depth 1, their children have depth 2, etc. 
 * </p>
 *
 * <p>Depth of the first element is 0. If the depth of a node is D, the depth of the next node in the array can be:</p>
 * <ul>
 *   <li>D + 1 if the next node is a child of this node;</li>
 *   <li>D if the next node is a sibling of this node;</li>
 *   <li>d < D - in this case the next node is not related to this node.</li>
 * </ul>
 *
 * <p>Example:</p>
 * <code>
 * nodeIds: 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11
 * depths: 0, 1, 2, 3, 1, 0, 1, 0, 1, 1, 2
 * the forest can be visualized as follows:
 * 1
 * - 2
 * - - 3
 * - - - 4
 * - 5
 * 6
 * - 7
 * 8
 * - 9
 * - 10
 * - - 11
 * </code>
 * Note that the depth is equal to the number of hyphens for each node.
 * */
interface Hierarchy {
  int size();

  int nodeId(int index);

  int depth(int index);

  default String formatString() {
    return IntStream.range(0, size()).mapToObj(i -> "" + nodeId(i) + ":" + depth(i) ).collect(Collectors.joining(", ", "[", "]"));
  }
}

class Filter {
  /**
   * A node is present in the filtered hierarchy iff its node ID passes the predicate and all of its ancestors pass it as well.
   * */
  static Hierarchy filter(Hierarchy hierarchy, IntPredicate nodeIdPredicate) {
    List<Integer> filteredNodeIds = new ArrayList<>();
    List<Integer> filteredDepths = new ArrayList<>();

    for (int i = 0; i < hierarchy.size(); i++) {
        int nodeId = hierarchy.nodeId(i);
        int depth = hierarchy.depth(i);

        // Check if the node ID passes the predicate and its ancestors pass it as well
        boolean includeNode = true;
        for (int j = 0; j <= i; j++) {
            if (!nodeIdPredicate.test(hierarchy.nodeId(j))) {
                includeNode = false;
                break;
            }
        }

        if (includeNode) {
            filteredNodeIds.add(nodeId);
            filteredDepths.add(depth);
        }
    }

    int[] filteredNodeIdsArray = filteredNodeIds.stream().mapToInt(Integer::intValue).toArray();
    int[] filteredDepthsArray = filteredDepths.stream().mapToInt(Integer::intValue).toArray();
    return new ArrayBasedHierarchy(filteredNodeIdsArray, filteredDepthsArray);
}
}

class ArrayBasedHierarchy implements Hierarchy {
  private final int[] myNodeIds;
  private final int[] myDepths;

  public ArrayBasedHierarchy(int[] nodeIds, int[] depths) {
    myNodeIds = nodeIds;
    myDepths = depths;
  }

  @Override
  public int size() {
    return myDepths.length;
  }

  @Override
  public int nodeId(int index) {
    return myNodeIds[index];
  }

  @Override
  public int depth(int index) {
    return myDepths[index];
  }
}

public class FilterTest {
  @Test
  public void testFilterWithDifferentPredicate() {
    Hierarchy unfiltered = new ArrayBasedHierarchy(
        new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11},
        new int[]{0, 1, 2, 3, 1, 0, 1, 0, 1, 1, 2}
    );
    Hierarchy filteredActual = Filter.filter(unfiltered, nodeId -> nodeId % 2 == 0);
    Hierarchy filteredExpected = new ArrayBasedHierarchy(
        new int[]{2, 6, 8, 10},
        new int[]{1, 0, 0, 1}
    );

    Assert.assertEquals(filteredExpected.formatString(), filteredActual.formatString());
  }
  
  @Test
  public void testFilterWithEmptyHierarchy() {
    Hierarchy unfiltered = new ArrayBasedHierarchy(new int[]{}, new int[]{});
    Hierarchy filteredActual = Filter.filter(unfiltered, nodeId -> nodeId % 2 == 0);
    Hierarchy filteredExpected = new ArrayBasedHierarchy(new int[]{}, new int[]{});

    Assert.assertEquals(filteredExpected.formatString(), filteredActual.formatString());
  }

  @Test
  public void testFilterWithSingleNode() {
    Hierarchy unfiltered = new ArrayBasedHierarchy(new int[]{1}, new int[]{0});
    Hierarchy filteredActual = Filter.filter(unfiltered, nodeId -> nodeId % 2 == 0);
    Hierarchy filteredExpected = new ArrayBasedHierarchy(new int[]{}, new int[]{});

    Assert.assertEquals(filteredExpected.formatString(), filteredActual.formatString());
  }

  @Test
  public void testFilterWithComplexPredicate() {
    Hierarchy unfiltered = new ArrayBasedHierarchy(
        new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12},
        new int[]{0, 1, 2, 3, 1, 0, 1, 0, 1, 1, 2, 1}
    );
    Hierarchy filteredActual = Filter.filter(unfiltered, nodeId -> nodeId % 4 == 0 || nodeId % 3 == 0);
    Hierarchy filteredExpected = new ArrayBasedHierarchy(
        new int[]{4, 8, 12},
        new int[]{3, 0, 1}
    );

    Assert.assertEquals(filteredExpected.formatString(), filteredActual.formatString());
  }
}
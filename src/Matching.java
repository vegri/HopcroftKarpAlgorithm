import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TreeSet;

class Vertex implements Comparable<Vertex> {
	public TreeSet<Vertex> adjacencyList = new TreeSet<>(); // list of linked
															// dates or students
	public String name;
	public Vertex match = null; // match date/student
	public int indegree = 0; // wtf. we need it. kind of.
	public int storedLevel = -1; // level number in which the vertex is stored
									// (-1 if not stored anywhere)

	public boolean isClassified() {
		return storedLevel != -1;
	}

	public boolean isStoredInAnyLevel() {
		return storedLevel >= 0;
	}

	public Vertex(String name) {
		this.name = name;
	}
	
	@Override
	public String toString(){
		return name;
	}

	// delete neighbour in adjacency list and vice versa
	public void deleteEdge(Vertex neighbour) {
		adjacencyList.remove(neighbour);
		neighbour.adjacencyList.remove(this);
	}

	@Override
	public int compareTo(Vertex o) {
		return name.compareTo(o.name);
	}

}

/**
 * Algorithms based on http://www.cin.ufpe.br/~hcs/if775/Hopcroft-Karp.pdf Thank
 * you very much!
 * 
 */
class HopcroftKarp {

	List<TreeSet<Vertex>> levels = new ArrayList<>();
	List<List<Vertex>> augmentingPaths = new ArrayList<>();

	public void findMatching(List<Vertex> students) {

		// add all free vertices from students to level 0
		for (Vertex student : students) {
			if (student.match == null) {
				if (levels.isEmpty()) {
					levels.add(new TreeSet<Vertex>());
				}
				levels.get(0).add(student);
			}
		}
		int i = 0;

		
		do{
			if (levels.size() < i + 2) {
				levels.add(new TreeSet<Vertex>());
			}
			// iterate through all students in current level
			for (Vertex student : levels.get(i)) {
				// store all possible dates for current student in next level if
				// not matched by student and not already stored
				for (Vertex freeDate : student.adjacencyList) {
					if (student.match == freeDate) {
						continue;
					}
					if (!freeDate.isStoredInAnyLevel()) {
						// store or overwrite freeDate at level i+1 (TreeSet
						// stores every element only once)
						levels.get(i + 1).add(freeDate);
						freeDate.storedLevel = i + 1;
					}
					student.indegree += 1;
				}
			}

			if (levelHasFreeVertex(i + 1)) {
				// delete all matched vertices in level i+1
				for (Vertex date : levels.get(i + 1)) {
					if (date.match != null) {
						levels.get(i + 1).remove(date);
						augmentation(i + 1);
					}
				}
			} else {
				// add level i+2
				if (levels.size() < i + 3) {
					levels.add(new TreeSet<Vertex>());
				}
				// add all matched vertices of level i+1 to level i+2 if not
				// already stored in any level
				for (Vertex date : levels.get(i + 1)) {
					if (!date.match.isStoredInAnyLevel()) {
						levels.get(i + 2).add(date.match);
						date.match.storedLevel = i + 2;
					}
				}

			}
			// go to next student level
			i += 2;
		}
		while (!isAllVerticesClassified(students) && !levelHasFreeVertex(i-1));
		System.out.println(augmentingPaths);
	}

	private boolean levelHasFreeVertex(int level) {

		for (Vertex date : levels.get(level)) {
			if (date.match == null) {
				return true;
			}
		}
		return false;
	}

	private boolean isAllVerticesClassified(List<Vertex> students) {

		for (Vertex student : students) {
			if (!student.isClassified()) {
				return false;
			}
		}
		return true;
	}

	private boolean findFreeVertexInFirstLevel(Vertex currentVertex, int level,
			List<Vertex> augmentingPath) {
		augmentingPath.add(currentVertex);
		if (level == 0) {
			if (currentVertex.match == null) {
				return true;
			}
			return false;
		}
		// find connected vertices of currentVertex in the next lower level
		for (Vertex vertex : currentVertex.adjacencyList) {
			if (levels.get(level - 1).contains(vertex)) {
				// start recursion: if path via current vertex led to free
				// vertex in level 0, return true
				if (findFreeVertexInFirstLevel(vertex, level - 1,
						augmentingPath)) {
					return true;
				}
				// recursive call added vertex to augmentingPath. remove it
				// again as vertex in level 0 was not free (try another path)
				augmentingPath.remove(augmentingPath.size() - 1);
			}
		}
		return false;
	}

	private void augmentation(int lastLevel) {
		List<Vertex> augmentingPath = new ArrayList<>();
		for (Vertex date : levels.get(lastLevel)) {
			if (findFreeVertexInFirstLevel(date, lastLevel, augmentingPath)) {
				break;
			}
		}

		for (int i = 0; i < augmentingPath.size(); i += 2) {
			augmentingPath.get(i).match = augmentingPath.get(i + 1);
			augmentingPath.get(i + 1).match = augmentingPath.get(i);
		}

		augmentingPaths.add(augmentingPath);
		Queue<Vertex> deleteVertices = new LinkedList<>(augmentingPath);
		while (!deleteVertices.isEmpty()) {
			Vertex deleteVertex = deleteVertices.poll();
			removeVertexFromLevel(deleteVertex, deleteVertices);

		}

	}

	private void removeVertexFromLevel(Vertex vertex,
			Queue<Vertex> deletionQueue) {
		vertex.match.match = null;
		vertex.match = null;
		levels.get(vertex.storedLevel).remove(vertex);
		// store as classified:
		vertex.storedLevel = -2;
		for (Vertex neighbour : vertex.adjacencyList) {
			neighbour.indegree--;
			vertex.deleteEdge(neighbour);
			if (neighbour.indegree == 0) {
				deletionQueue.add(neighbour);
			}
		}
	}

}

public class Matching {

	public static void main(String[] args) {

		Vertex a = new Vertex("a");
		Vertex d1 = new Vertex("1");
		a.adjacencyList.add(d1);

		List<Vertex> students = new ArrayList<>();
		students.add(a);

		HopcroftKarp hkAlgorithm = new HopcroftKarp();
		hkAlgorithm.findMatching(students);

	}

}

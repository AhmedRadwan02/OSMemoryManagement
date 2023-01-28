import java.util.*;

public class Test {
	static int currBase = 0;
	static int currUsed = 0;

	public static void main(String[] args) {

		Scanner in = new Scanner(System.in);
		ArrayList<Process> processList = new ArrayList<Process>();
		System.out.println("Enter Memory Maximum Size: ");
		int max = in.nextInt();
		String cmd = "";
		// RQ RL C STAT
		while (!cmd.equalsIgnoreCase("EXIT")) {
			PrintMainPage();
			cmd = in.next();
			if (cmd.equalsIgnoreCase("Exit")) {
				System.exit(0);
			} else if (cmd.equalsIgnoreCase("RQ")) {
				AllocMemory(processList, max, in);
				currBase++;
			} else if (cmd.equalsIgnoreCase("RL")) {
				// change the process if was there to unused
				deAllocMemory(processList, in);

			} else if (cmd.equalsIgnoreCase("C")) {
				int freeSize = 0;
				// delete unused
				for (int k = 0; k < processList.size(); k++) {
					for (int i = 0; i < processList.size(); i++) {
						if (processList.get(i).getName().equalsIgnoreCase("Unused")) {
							freeSize += processList.get(i).getSize();
							currUsed -= processList.get(i).getSize();
							for (int j = i + 1; j < processList.size(); j++) {
								processList.get(j)
										.setBaseStart(processList.get(j).getBaseStart() - processList.get(i).getSize());
							}
							processList.remove(i);
						}
					}
				}
				processList.add(new Process("Unused", freeSize));
				processList.get(processList.size() - 1)
						.setBaseStart(((processList.get(processList.size() - 2).getSize())
								+ (processList.get(processList.size() - 2).getBaseStart())));
			} else if (cmd.equalsIgnoreCase("STAT")) {
				for (Process p : processList) {
					System.out.println("Address [ " + p.getBaseStart() + " - " + (p.getBaseStart() + p.getSize()) + "] "
							+ p.getName());
				}
			}

		}
	}

	// --------- Main Page ---------------
	public static void PrintMainPage() {
		System.out.println("------------------------------------------------------------------------");
		System.out.println("\t1- RQ : Request for a contiguous block of memory.");
		System.out.println("\t2- RL : Release a contiguous block of memory.");
		System.out.println("\t3- C : Compact unused holes of memory into one single block.");
		System.out.println("\t4- STAT : Report the regions of free and allocated memory.");
		System.out.println("\t5- EXIT : Close System.");
		System.out.println("------------------------------------------------------------------------\n");
	}

	// --------- Allocate Memory ---------
	public static void AllocMemory(ArrayList<Process> pList, int max, Scanner in) {
		// if the memory was full , u cannot allocate
		if (currUsed == max) {
			System.out.println("Memory is Full cannot Request.");
			return;
		}
		// if it is still less than max even by 1
		// RQ ( done ) P0 40000 W
		System.out.print("Enter Process Name , Size , flag (e.g P0 40000 W): ");
		String name = in.next();
		int size = in.nextInt();
		String flag = in.next();
		// Create process either way allocated or not
		Process p = new Process(name, size);
		// F B W - first best worst
		// ------------------- First allocation ----------------------------
		if (flag.equalsIgnoreCase("f")) {
			for (Process process : pList) {
				if (process.getName().equalsIgnoreCase("Unused") && process.getSize() >= p.getSize()) {
					// if it was partitioned we do not change the size
					process.setName(p.getName());
				}
			}
			// if there is no unused look for the memory
			if (p.getSize() < (max - currUsed)) {
				pList.add(p);
				p.setBaseStart(currBase);
				currBase += p.getSize();
				currUsed += p.getSize();
			}
			// ------------------- Best allocation ----------------------------
		} else if (flag.equalsIgnoreCase("B")) {
			// look for available choose the minimum
			// minimum of available
			int minInd = minProcesses(pList, p.getSize(), max);
			if (minInd == -1 && p.getSize() <= (max - currUsed)) {
				// will be assigned from memory
				pList.add(p);
				p.setBaseStart(currBase);
				currBase += p.getSize();
				currUsed += p.getSize();
			} else if (p.getSize() <= pList.get(minInd).getSize()) {
				pList.get(minInd).setName(p.getName());
			} else {
				System.out.println("No available memory");
			}

			// ------------------- Worst allocation ----------------------------
		} else {
			int maxInd = maxProcesses(pList, max, currBase);
			if (maxInd == -1 && p.getSize() <= (max - currUsed)) {
				// will be assigned from memory
				pList.add(p);
				p.setBaseStart(currBase);
				currBase += p.getSize();
				currUsed += p.getSize();
			} else if (p.getSize() <= pList.get(maxInd).getSize()) {
				pList.get(maxInd).setName(p.getName());
			} else {
				System.out.println("No available memory");
			}
		}

	}

	// -------- Sum Of All process --------------
	public static int sumProcesses(ArrayList<Process> pList) {
		int sum = 0;
		for (int i = 0; i < pList.size(); i++)
			sum += pList.get(i).getSize();

		return sum;
	}

	// -------- Index of Maximum --------------------
	public static int maxProcesses(ArrayList<Process> pList, int max, int currBase) {
		int maxAvailable = max - currBase;
		// if was -1 then it is in free memory
		int maxInd = -1;
		for (int i = 0; i < pList.size(); i++) {
			if (pList.get(i).getSize() > maxAvailable && pList.get(i).getName().equalsIgnoreCase("Unused")) {
				maxAvailable = pList.get(i).getSize();
				maxInd = i;
			}
		}

		return maxInd;

	}

	// -------- Index of Minimum Available --------------------
	public static int minProcesses(ArrayList<Process> pList, int processSize, int max) {
		int minIndex = -1;
		int minAvailable = Integer.MAX_VALUE;
		for (int i = 0; i < pList.size(); i++) {
			if (pList.get(i).getSize() < minAvailable && pList.get(i).getSize() >= processSize
					&& pList.get(i).getName().equalsIgnoreCase("Unused")) {
				minIndex = i;
				minAvailable = pList.get(minIndex).getSize();
			}
		}
		if ((max - currBase) >= processSize && (max - currBase) < minAvailable)
			minIndex = -1;
		return minIndex;

	}

	// ---------- Deallocate Memory --------------
	public static void deAllocMemory(ArrayList<Process> pList, Scanner in) {
		System.out.println("Enter the process name: ");
		String name = in.next();
		for (Process p : pList) {
			if (p.getName().equalsIgnoreCase(name)) {
				p.setName("UnUsed");
				currUsed -= p.getSize();
			}
		}
	}

}

//--------- Process Class --------------------------
class Process {

	private String name;
	private int size;
	private int baseStart;

	// Constructor
	public Process(String name, int size) {
		this.name = name;
		this.size = size;
	}

	// ---------------
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getBaseStart() {
		return baseStart;
	}

	public void setBaseStart(int baseStart) {
		this.baseStart = baseStart;
	}

}

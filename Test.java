import java.util.*;

public class Test {
	static int currBase = 0;
	static int currUsed = 0;

	public static void main(String[] args) {

		Scanner in = new Scanner(System.in);
		ArrayList<Process> processList = new ArrayList<Process>();
		System.out.print("Enter Memory Maximum Size: ");
		int max = 0;
		while (max <= 0) {
			try {
				max = in.nextInt();
			} catch (InputMismatchException e) {
				System.out.println("Enter a Positive Integer!");
				in.next();
				continue;
			}
		}
		String cmd = "";
		// RQ RL C STAT
		while (!cmd.equalsIgnoreCase("EXIT")) {
			PrintMainPage();
			cmd = in.next();
			if (cmd.equalsIgnoreCase("Exit")) {
				System.exit(0);
			} else if (cmd.equalsIgnoreCase("RQ")) {

				if (currUsed >= max && !hasUnUsed(processList)) {
					System.out.println("Memory is full; current used is " + currUsed + " out of " + max);
				} else {
					AllocMemory(processList, max, in);
				}
			} else if (cmd.equalsIgnoreCase("RL")) {
				// change the process if was there to unused
				deAllocMemory(processList, in);

			} else if (cmd.equalsIgnoreCase("C")) {

				for (int i = 0; i < processList.size(); i++) {
					if (processList.get(i).getName().equalsIgnoreCase("UnUsed")) {
						// if u found unused , add it is neighbors to it and delete it
						// do not increment j since we are removing from the list
						for (int j = i + 1; j < processList.size();) {
							if (processList.get(j).getName().equalsIgnoreCase("UnUsed")) {
								processList.get(i).setSize(processList.get(i).getSize() + processList.get(j).getSize());
								processList.remove(processList.get(j));
							} else {
								break;
							}

						}
					}
				}

			} else if (cmd.equalsIgnoreCase("STAT")) {
				for (Process p : processList) {
					System.out.println("Address [ " + p.getBaseStart() + " - " + (p.getBaseStart() + p.getSize() - 1)
							+ "] " + p.getName());
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
		if (flag.equalsIgnoreCase("F")) {

			for (Process process : pList) {
				if (process.getName().equalsIgnoreCase("Unused") && process.getSize() >= p.getSize()) {
					// if it was partitioned - take only the needed size
					process.setName(p.getName());
					if (p.getSize() < process.getSize()) {
						pList.add(pList.indexOf(process) + 1, new Process("UnUsed", process.getSize() - p.getSize()));
						pList.get(pList.indexOf(process) + 1).setBaseStart(process.getBaseStart() + p.getSize());
					}
					process.setSize(p.getSize());
					return;
				}
			}
			// if there is no unused look for the memory
			if (p.getSize() <= (max - currUsed) && !pList.contains(p)) {
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
				if (p.getSize() < pList.get(minInd).getSize()) {
					pList.add(minInd + 1, new Process("UnUsed", pList.get(minInd).getSize() - p.getSize()));
					pList.get(minInd + 1).setBaseStart(pList.get(minInd).getBaseStart() + pList.get(minInd).getSize());
				}
				pList.get(minInd).setSize(p.getSize());
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
				if (p.getSize() < pList.get(maxInd).getSize()) {
					pList.add(maxInd + 1, new Process("UnUsed", pList.get(maxInd).getSize() - p.getSize()));
					pList.get(maxInd + 1).setBaseStart(pList.get(maxInd).getBaseStart() + pList.get(maxInd).getSize());
				}
				pList.get(maxInd).setSize(p.getSize());
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
			}
		}
	}

	// -------- Check Unused ----------------------
	public static boolean hasUnUsed(ArrayList<Process> pList) {
		boolean flag = false;
		for (Process p : pList) {
			if (p.getName().equalsIgnoreCase("UnUsed")) {
				flag = true;
			}
		}
		return flag;
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

package work;

public class FlowThread extends Thread {
	/*@OVERVIEW:
	 *流量处理线程类，接收出租车的增加流量信息，以及为出租车定时发送当前
	 *道路的流量方法，每经过200ms流量数组进行一次更新，为出租车的判断提供依据
	 * */
	private int flow[][];
	private Taxi taxiArray[];

	public FlowThread(Taxi taxiArray[], int flow[][]) {
		/*@ REQUIRES: None;
		@ MODIFIES: this;
		@ EFFECTS: 构造函数，传递线程所需参数，进行数据赋值
		*/
		this.taxiArray = taxiArray;
		this.flow = flow;
	}

	public boolean repOK() {
		/*@ REQUIRES: taxiArray!=null && taxiArray.length==100 
		@ MODIFIES: None;
		@ EFFECTS: 若符合类所需的参数条件，则返回true，否则返回false
		*/
		if (taxiArray == null || taxiArray.length != 100)
			return false;
		return true;
	}

	@Override
	public void run() {
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 先将当前的流量数组传入每个出租车，供出租车进行路径判断；
		之后经过0.2秒初始化流量数组，线程保证一直在执行
		*/
		try {
			while (true) {
				sendFlow(taxiArray);
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				initFlow();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void receiveChangeFlow(int start, int end) {
		/*@ REQUIRES: start为要进行运动时的出发点对应的位置，end为要进行运动时的终点对应的位置;
		@ MODIFIES: this;
		@ EFFECTS: 对线程的流量数组进行更新，每当有出租车在该道路移动时，流量增加1
		*/
		flow[start][end]++;
		flow[end][start]++;
	}

	private void initFlow() {
		/*@ REQUIRES: None;
		@ MODIFIES: this;
		@ EFFECTS: 对流量数组进行初始化
		*/
		for (int i = 0; i < flow.length; i++) {
			for (int j = 0; j < flow.length; j++) {
				flow[i][j] = 0;
			}
		}
	}

	private void sendFlow(Taxi taxiArray[]) {
		/*@ REQUIRES: taxiArray是存放出租车的数组;
		@ MODIFIES: None;
		@ EFFECTS: 将流量数组传递给每一辆出租车，传递流量数组参数
		*/
		for (int i = 0; i < taxiArray.length; i++) {
			taxiArray[i].getFlow(flow);
		}
	}

	public int[][] getFlow() {
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 返回flow流量数组
		*/
		return flow;
	}
}

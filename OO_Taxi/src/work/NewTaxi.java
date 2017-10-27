package work;

import java.util.List;
import java.util.ListIterator;

public class NewTaxi extends Taxi {
	/*@OVERVIEW:
	 *可追踪出租车线程类，出租车在空闲阶段，保持随机运动，并且在到达一定时间后，将停止运动；
	 *在接收响应的用户请求后，对其进行响应，并且进行位置更新以及信用增加等。同时具有将信息
	 *输出到文件的功能，支持GUI的实时更新以及与其他线程之间发生数据的传递，具体的追踪信息也
	 *可通过迭代进行控制台输出
	 * */
	public NewTaxi(int id, int[] position, List<Request> requestList, int[][] weight, int[][] map, TaxiGUI gui,
			FlowThread flowThread, int[][] light, int[][] lightControl, int SleepTime) {
		super(id, position, requestList, weight, map, gui, flowThread, light, lightControl, SleepTime);
		gui.SetTaxiType(id, 1);
	}

	public boolean repOK() {
		/*@ REQUIRES:   0<=id<=99  &&  0<=position[0]<=79 && 0<=position[1]<=79  
		 * && weight!=null &&  map!=null && gui!=null && flowThread!=null && light!=null
		 * && lightControl!=null 
		@ MODIFIES: None;
		@ EFFECTS: 若符合类所需的参数条件，则返回true，否则返回false
		*/
		if (id < 0 || id >= 100)
			return false;
		if (position[0] < 0 || position[0] > 79 || position[1] < 0 || position[1] > 79)
			return false;
		if (weight == null)
			return false;
		if (map == null)
			return false;
		if (gui == null)
			return false;
		if (flowThread == null)
			return false;
		if (light == null)
			return false;
		if (lightControl == null)
			return false;
		return true;
	}

	@Override
	public void run() {
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 执行父类的run方法，保证出租车的运动，在抢到单时，进行相应处理，否则按照规定进行运动与停止；
		在进行抢到单处理时，时刻输出信息到控制台与文件中，抢单后清空出租车当前请求，而后继续运行
		*/
		super.run();
	}

	public List<Request> getDealRequestList() {
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 返回dealRequestList请求列表
		*/
		return dealRequestList;
	}

	public int getRequestListSize() {
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 返回dealRequestList请求列表的大小
		*/
		return dealRequestList.size();
	}

	public Request getRequest(int index) {
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 返回dealRequestList请求列表中index对应的请求项
		*/
		return dealRequestList.get(index);
	}

	public String getMovePath(int index) {
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 返回dealRequestStringList路径记录列表中index对应的路径项
		*/
		return dealRequestStringList.get(index);
	}

	public int getPosition(int index) {
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 返回positionIndexList出租车抢到单地点记录列表中index对应的记录
		*/
		return positionIndexList.get(index);
	}

	public void iteratorBothDirection() {
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 对出租车的抢到单的请求进行迭代处理，正向与反向迭代均实现，在控制台输出响应信息
		*/
		ListIterator<Request> iterList = dealRequestList.listIterator();
		int i = 0;
		System.out.println("正向迭代结果:");
		while (iterList.hasNext()) {
			Request request = iterList.next();
			double requestTime = request.getRequestTime();
			int src[] = request.getSrc();
			int dst[] = request.getDst();
			int taxiPosition[] = changeToPositon(positionIndexList.get(i));
			String path = dealRequestStringList.get(i);
			i++;
			System.out.println("requestTime=" + requestTime + "	请求发出地=(" + src[0] + "," + src[1] + ")	请求目的地=("
					+ dst[0] + "," + dst[1] + ")	出租车抢单所在地=(" + taxiPosition[0] + "," + taxiPosition[1]
					+ ")	出租车去接乘客以及运送乘客去目的地途中所行驶的路径:" + path);
		}
		i--;
		System.out.println("反向迭代结果:");
		while (iterList.hasPrevious()) {
			Request request = iterList.previous();
			double requestTime = request.getRequestTime();
			int src[] = request.getSrc();
			int dst[] = request.getDst();
			int taxiPosition[] = changeToPositon(positionIndexList.get(i));
			String path = dealRequestStringList.get(i);
			i--;
			System.out.println("requestTime=" + requestTime + "	请求发出地=(" + src[0] + "," + src[1] + ")	请求目的地=("
					+ dst[0] + "," + dst[1] + ")	出租车抢单所在地=(" + taxiPosition[0] + "," + taxiPosition[1]
					+ ")	出租车去接乘客以及运送乘客去目的地途中所行驶的路径:" + path);
		}
	}
}

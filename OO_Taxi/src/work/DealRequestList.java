package work;

import java.util.ArrayList;
import java.util.List;

public class DealRequestList extends Thread{
	/*@OVERVIEW:
	 *处理用户指令的线程类，对队列中要被取出的用户指令进行出租车的查询，
	 *若有满足条件的出租车，则向该出租车发送该指令让其进行响应；若没有，
	 *在控制台输出没有响应的提示信息。
	 * */
	public static final int Stop=0;
	public static final int WaitServe=2;
	public static final int GotoSrc=3;
	public static final int Serve=1;
	private List<Request> requestList=new ArrayList<Request>();
	private int weight[][];
	private int map[][];
	private Taxi taxiArray[];
	private  FlowThread flowThread;
	public DealRequestList(List<Request> requestList,int weight[][],int map[][],Taxi taxiArray[],FlowThread flowThread) {
		/*@ REQUIRES: requestList中的命令合法，其余参数均合法即可;
		@ MODIFIES:this;
		@ EFFECTS: 将请求队列中的请求执行后，移出requestList,对flowThread线程的flow流量数组更新
		*/
		this.requestList = requestList;
		this.weight=weight;
		this.map=map;
		this.taxiArray=taxiArray;
		this.flowThread=flowThread;
	}
	public boolean repOK(){
		/*@ REQUIRES: taxiArray!=null && taxiArray.length==100
		 *  &&flowThread!=null && map!=null && weight!=null ;
		@ MODIFIES: None;
		@ EFFECTS: 若符合类所需的参数条件，则返回true，否则返回false
		*/
		if(taxiArray==null || taxiArray.length!=100) return false;
		if(map==null) return false;
		if(weight==null) return false;
		if(flowThread==null) return false;
		return true;
	}
	private int changeToIndex(int position[]){
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 将position数组转换成对应权重数组中的坐标，返回坐标数组
		*/
		return position[0]*map.length+position[1];
	}
	private Taxi findBestTaxi(Request request){
		/*@ REQUIRES: request为合法请求指令;
		@ MODIFIES: this;
		@ EFFECTS: 对请求request进行最合适的出租车寻找，若找到则返回找到的出租车给调用者；
		若没有找到，则返回null给调用者。
		*/
		List<Taxi> requestTaxiList=request.getRequestTaxiList();
		Dijkstra dijkstra=new Dijkstra();
		List<Taxi> tempCreditTaxiList=new ArrayList<Taxi>();
		int credit=0;
		int minDistance=10000;
		int index=0;
		//去除不属于等待服务状态的所有出租车
		for(int i=0;i<requestTaxiList.size();i++){
			if(requestTaxiList.get(i).getStatus()!=WaitServe){
				requestTaxiList.remove(i);
			}
		}
		if(requestTaxiList.isEmpty()){
			return null;
		}
		//设置最大的credit
		for(int i=0;i<requestTaxiList.size();i++){
			Taxi taxi=requestTaxiList.get(i);
			if(taxi.getCredit()>credit){
				credit=taxi.getCredit();
			}
		}
		//找到最大的credit的所有出租车集合
		for(int i=0;i<requestTaxiList.size();i++){
			Taxi taxi=requestTaxiList.get(i);
			if(taxi.getCredit()==credit){
				tempCreditTaxiList.add(taxi);
			}
		}
		//找出最大credit的集合中路径最短的
		int [] tempPath=new int [100];
		for(int i=0;i<tempCreditTaxiList.size();i++){
			Taxi taxi=tempCreditTaxiList.get(i);
			int Vstart=changeToIndex(taxi.getPosition());
			int Vend=changeToIndex(request.getSrc());
			int length=0;
			 int[] shortPath = dijkstra.findByDijsktra(weight,flowThread.getFlow(),Vstart, Vend);
			if((length=getLength(Vstart, Vend, shortPath))<minDistance){
				minDistance=length;
			}
			//记录对应的最短路径，加快效率
			tempPath[i]=length;
		}
		for(int i=0;i<tempCreditTaxiList.size();i++){
			Taxi taxi=tempCreditTaxiList.get(i);
			//如果credit与最短路径都满足，即使有多个，为了提高效率，也直接返回第一个。
			if(tempPath[i]==minDistance){
				index=i;
			}
		}
		tempCreditTaxiList.get(index).setStatus(GotoSrc);
		return tempCreditTaxiList.get(index);
	}
	private int getLength(int Vstart,int Vend,int shortPath[]){
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 得到记录前驱节点数组shortPath的真实长度，返回长度
		*/
		int v=Vend;
		int path[]=new int[weight.length];
		int i=0;//记录长度
		//倒序放入path数组
		while(v!=Vstart){
			path[i]=v;
			i++;
			v=shortPath[v];
		}
		path[i]=Vstart;
		i++;
		return i;
	}
	@Override
	public void run() {
		/*@ REQUIRES: None;
		@ MODIFIES: this;
		@ EFFECTS: 时刻对每个要被执行的请求进行处理，若该请求可以被执行，则为最合适的出租车传递该请求；
		若请求不可被执行，则进行相关信息的输出；最终要在请求队列中去掉该请求
		*/
		try {
			while(true){
				if(!requestList.isEmpty()){
					Request request=requestList.get(0);
					double time=request.getRequestTime();
					//丢掉该命令
					if(System.currentTimeMillis()/1000.0-time>=3.0){
						System.out.println("取出指令："+request);
						if(request.getRequestTaxiList().isEmpty()){
							System.out.println("请求为"+request+"的乘客请求未被响应!");
						}
						else{
							//筛选出最优出租车
							Taxi taxi=findBestTaxi(request);
							if(taxi==null){
								System.out.println("请求为"+request+"的乘客请求未被响应,可响应的出租车均不处于等待服务状态!");
							}
							else if(taxi!=null){
								//根据出租车id找寻对应的出租车
								taxiArray[taxi.getid()].receiveRequest(request);
							}
						}
						//移除该请求
						requestList.remove(request);
					}
				}
				else{
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}

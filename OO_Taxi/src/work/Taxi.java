package work;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
//status=0 停止运行状态 status=1服务状态 status=2等待服务状态  status=3接单状态
public class Taxi extends Thread{
	/*@OVERVIEW:
	 *出租车线程类，出租车在空闲阶段，保持随机运动，并且在到达一定时间后，将停止运动；
	 *在接收响应的用户请求后，对其进行响应，并且进行位置更新以及信用增加等。同时具有将信息
	 *输出到文件的功能，支持GUI的实时更新以及与其他线程之间发生数据的传递
	 * */
	protected static final int EWGREAN=1;
	protected static final int SNGREAN=2;
	protected static final int NORTH=1;
	protected static final int SOUTH=2;
	protected static final int WEST=3;
	protected static final int EAST=4;
	protected static final int Stop=0;
	protected static final int WaitServe=2;
	protected static final int GotoSrc=3;
	protected static final int Serve=1;
	protected String PathString="";
	protected Dijkstra dijkstra=new Dijkstra();
	protected int id;
	protected int position[]=new int[2];
	protected int status;
	protected int credit;
	protected List<Request> requestList=new ArrayList<Request>();//总的请求列表
	protected List<Request> taxiRequestList=new ArrayList<Request>();//抢单的所有响应命令
	protected List<Request> dealRequestList=new ArrayList<Request>();//存放处理过的抢到单的所有命令，查询使用
	protected List<String> dealRequestStringList=new ArrayList<String>();//存放每条处理过的抢到单的命令的服务的经过路径
	protected List<Integer> positionIndexList=new ArrayList<Integer>();//存放出租车在抢到单时的所处位置
	protected Request request=null;//处理的请求，每次只能处理一条
	protected int weight[][];
	protected int map[][];
	protected TaxiGUI gui;
	protected int flow[][]=null;
	protected BufferedWriter bw;
	protected boolean mapIsChanged;
	protected  FlowThread flowThread;
	protected int light[][];
	protected int lightControl[][];
	protected int SleepTime;
	protected int preposition=0;
	protected int first=0;
	public Taxi(int id,int position[],List<Request> requestList,int weight[][],int map[][],TaxiGUI gui,FlowThread flowThread,int light[][],int lightControl[][],int SleepTime) {
		/*@ REQUIRES: None;
		@ MODIFIES: this;
		@ EFFECTS: 构造函数，传递出租车需要的各种参数，为数据赋值，同时设置普通出租车在gui中的类型
		*/
		this.id=id;
		this.position=position;
		this.status=WaitServe;
		this.requestList=requestList;
		this.credit=0;
		this.weight=weight;
		this.map=map;
		this.gui=gui;
		this.flowThread=flowThread;
		this.mapIsChanged=false;
		this.light=light;
		this.lightControl=lightControl;
		this.SleepTime=SleepTime;
		gui.SetTaxiType(id,0);
	}
	public boolean repOK(){
		/*@ REQUIRES:   0<=id<=99  &&  0<=position[0]<=79 && 0<=position[1]<=79  
		 * && weight!=null &&  map!=null && gui!=null && flowThread!=null && light!=null
		 * && lightControl!=null 
		@ MODIFIES: None;
		@ EFFECTS: 若符合类所需的参数条件，则返回true，否则返回false
		*/
		if(id<0 || id>=100)  return false;
		if(position[0]<0 || position[0]>79 || position[1]<0 || position[1]>79) return false;
		if(weight==null) return false;
		if(map==null) return false;
		if(gui==null) return false;
		if(flowThread==null) return false;
		if(light==null) return false;
		if(lightControl==null) return false;
		return true;
	}
	@Override
	public void run() {
		/*@ REQUIRES: None;
		@ MODIFIES: this;
		@ EFFECTS: 保证出租车的运动，在抢到单时，进行相应处理，否则按照规定进行运动与停止；
		在进行抢到单处理时，时刻输出信息到控制台与文件中，抢单后清空出租车当前请求，而后继续运行
		*/
		try {
			//20s 停1s，则应count==(20/0.2=100)时候进入停止状态
			try {
				bw=new BufferedWriter(new FileWriter("taxi_"+id+".txt"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			int count=0;
			while(true){
				//出租车未曾抢到单
				if(request==null){
					if(count==100){
						Stop();
						count=0;
					}
					count+=1;
					lookUpRequestListAndRandomMove();
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				else{
					System.out.println("打印用户请求开始时，4*4范围内所有出租车状态、信用信息:");
					bw.write("打印用户请求开始时，4*4范围内所有出租车状态、信用信息:"+System.getProperty("line.separator"));
					bw.flush();
					for(int i=0;i<request.getRequestInregionTaxiList().size();i++){
//						Taxi taxi=request.getRequestInregionTaxiList().get(i);
						String string=request.getRequestInregionTaxiList().get(i);
						System.out.println(string);
						bw.write(string+System.getProperty("line.separator"));
						bw.flush();
					}
					System.out.println("打印抢单时间窗内所有抢单的出租车状态、信用信息:");
					bw.write("打印抢单时间窗内所有抢单的出租车状态、信用信息:"+System.getProperty("line.separator"));
					bw.flush();
					//打印抢单时间窗内所有抢单的出租车
					for(int i=0;i<request.getRequestTaxiList().size();i++){
						Taxi taxi=request.getRequestTaxiList().get(i);
						System.out.println("taxi id="+taxi.getid()+"   status="+taxi.getStatus()+"   credit=   "+taxi.getCredit());
						bw.write("taxi id="+taxi.getid()+"   status="+taxi.getStatus()+"   credit=   "+taxi.getCredit()+System.getProperty("line.separator"));
						bw.flush();
					}
					//出发前往乘客打车地
					System.out.println("出租车"+id+"抢到单:"+request);
					bw.write("出租车"+id+"抢到单:"+request+System.getProperty("line.separator"));
					bw.flush();
					moveToPassengerSrc(request);
					//前往乘客的目的地
					moveToPassengerDst(request);
					dealRequestStringList.add(PathString);
					PathString="";
					count=0;
					dealRequestList.add(request);
					request=null;
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	public int changeToIndex(int position[]){
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 将传入的位置数组转换成权重数组下标，返回下标
		*/
		return position[0]*map.length+position[1];
	}
	public int [] changeToPositon(int position){
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 将传入权重数组下标转换为位置数组，返回位置数组
		*/
		int array[]=new int[2];
		array[0]=position/map.length;
		array[1]=position%map.length;
		return array;
	}
	public void updatePosition(int position[],int v){
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 更新出租车的实时位置，将位置按照传入的参数进行更新
		*/
		position[0]=v/map.length;
		position[1]=v%map.length;
	}
	public void receiveMapChange(){
		/*@ REQUIRES: None;
		@ MODIFIES: this;
		@ EFFECTS: 接收是否有地图修改请求输入，若有，设置响应变量为true
		*/
		this.mapIsChanged=true;
	}
	//	停止1s
	public void Stop(){
		/*@ REQUIRES: None;
		@ MODIFIES: this;
		@ EFFECTS: 出租车的暂停1s的方法，保证gui上出租车位置的更新
		*/
		//设置状态
		setStatus(Stop);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		};
		Point point=new Point(position[0], position[1]);
		gui.SetTaxiStatus(id, point, status);
	}
	//出发前往乘客打车地
	public void moveToPassengerSrc(Request request){
		/*@ REQUIRES: None;
		@ MODIFIES: this;
		@ EFFECTS: 出租车前往乘客打车地，途经道路信息输出到控制台与文件，调用moveAsDijkstra()方法
		来获得下一步出租车要运动的点的坐标，直到到达乘客打车地
		*/
		setStatus(GotoSrc);
		int Vend=changeToIndex(request.getSrc());
		int Vstart=changeToIndex(position);
		int array[]=null;
		System.out.println("出租车"+id+"在去用户请求发出地的过程中经过的路径:");
		System.out.print("("+position[0]+","+position[1]+")");
		PathString=PathString.concat("("+position[0]+","+position[1]+")");
		positionIndexList.add(changeToIndex(position));
		try {
			bw.write("出租车"+id+"在去用户请求发出地的过程中经过的路径:"+System.getProperty("line.separator"));
			bw.write("("+position[0]+","+position[1]+")");
			bw.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		//在没有更改地图之前执行完毕，则返回null,否则执行继续按照moveAsDijkstra执行
		while((array=moveAsDijkstra(Vstart,Vend))!=null){
			Vstart=array[0];
			Vend=array[1];
			array=null;
		}
		//到达乘客请求地后，停止1s
		setStatus(Stop);
		int time=0;
		while(time<5){
			time++;
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Point point=new Point(position[0], position[1]);
			gui.SetTaxiStatus(id, point, status);
			setStatus(Stop);
		}
	}
	//前往乘客的目的地
	public void moveToPassengerDst(Request request){
		/*@ REQUIRES: None;
		@ MODIFIES: this;
		@ EFFECTS: 出租车前往乘客目的地，途经道路信息输出到控制台与文件，调用moveAsDijkstra()方法
		来获得下一步出租车要运动的点的坐标，直到到达乘客目的地
		*/
		setStatus(Serve);
		int Vend=changeToIndex(request.getDst());
		int Vstart=changeToIndex(position);
		int array[]=null;
		System.out.println("出租车"+id+"在去用户目的地的过程中经过的路径:");
		System.out.print("("+position[0]+","+position[1]+")");
		try {
			bw.write("出租车"+id+"在去用户目的地的过程中经过的路径:"+System.getProperty("line.separator"));
			bw.write("("+position[0]+","+position[1]+")");
			bw.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		//在没有更改地图之前执行完毕，则返回null,否则执行继续按照moveAsDijkstra执行
		while((array=moveAsDijkstra(Vstart,Vend))!=null){
			Vstart=array[0];
			Vend=array[1];
			array=null;
		}
		//到达目的地后，停止1s
		addCredit(3);
		System.out.println("出租车   "+id+"   到达乘客目标地点"+"("+Vend/map.length+","+Vend%map.length+"),当前信用为"+credit);
		try {
			bw.write("出租车   "+id+"   到达乘客目标地点"+"("+Vend/map.length+","+Vend%map.length+"),当前信用为"+credit+System.getProperty("line.separator"));
			bw.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		setStatus(Stop);
		int time=0;
		while(time<5){
			time++;
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Point point=new Point(position[0], position[1]);
			gui.SetTaxiStatus(id, point, status);
			setStatus(Stop);
		}
		//回到等待服务状态
		setStatus(WaitServe);
	}
	private boolean lightAllowsMove(int [] preposition,int [] position){
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 对于出租车的初次运动，判断出租车所在该点的红绿灯是否允许出租车进行行驶，
		若允许出租车行驶，返回true；否则返回false；
		*/
		//若没有红绿灯控制
		if(light[preposition[0]][preposition[1]]==0){
			return true;
		}
		else if(light[preposition[0]][preposition[1]]==1){
			int direction=0;//上1，下2，左3，右4
			//同一列，分上下
			if(preposition[1]==position[1]){
				direction=preposition[0]>position[0] ? NORTH : SOUTH; 
			}
			//同一行，分左右
			else if(preposition[0]==position[0]){
				direction=preposition[1]>position[1] ? WEST : EAST; 
			}
			//确定方向后进行红绿灯判断
			int lightSign=lightControl[preposition[0]][preposition[1]];
			//返回所有可以运行的情况
			//可以左右行驶
			if(lightSign==EWGREAN){
				if(direction==WEST || direction==EAST){
					return true;
				}
			}
			else if(lightSign==SNGREAN){
				if(direction==SOUTH || direction==NORTH){
					return true;
				}
			}
			else{
				return false;
			}
		}
		return false;
	}
	public boolean lightAllowsMoveThen(int [] prepreposition,int [] preposition,int [] position){
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 对出租车的之前的位置，现在的位置以及将要运动到的下一个位置进行判断，结合出租车的运动方向以及红绿灯
		道口的信号灯种类，根据指导书的要求判断出租车是否能行驶，若能，返回true；否则返回false；
		*/
		//考虑转弯情况
		//若没有红绿灯控制
				if(light[preposition[0]][preposition[1]]==0){
					return true;
				}
				else if(light[preposition[0]][preposition[1]]==1){
					int predirection=0,direction=0;//上1，下2，左3，右4
					//同一列，分上下
					if(prepreposition[1]==preposition[1]){
						predirection=prepreposition[0]>preposition[0] ? NORTH : SOUTH; 
					}
					//同一行，分左右
					else if(prepreposition[0]==preposition[0]){
						predirection=preposition[1]>position[1] ? WEST : EAST; 
					}
					//同一列，分上下
					if(preposition[1]==position[1]){
						direction=preposition[0]>position[0] ? NORTH : SOUTH; 
					}
					//同一行，分左右
					else if(preposition[0]==position[0]){
						direction=preposition[1]>position[1] ? WEST : EAST; 
					}
					//先判断右转
					if((predirection==NORTH && direction==EAST) || (predirection==SOUTH && direction==WEST) || 
							(predirection==WEST && direction==NORTH) || (predirection==EAST && direction==SOUTH)){
						return true;
					}
					//绿灯亮，可直行；红灯亮，可左转弯
					//确定方向后进行红绿灯判断
					int lightSign=lightControl[preposition[0]][preposition[1]];
					if(lightSign==EWGREAN){
						if(direction==EAST || direction==WEST || (predirection==NORTH && direction==WEST) || (predirection==SOUTH && direction==EAST)){
							return true;
						}
					}
					else if(lightSign==SNGREAN){
						if(direction==NORTH || direction==SOUTH || (predirection==WEST && direction==SOUTH) || (predirection==EAST && direction==NORTH)){
							return true;
						}
					}
				}
		return false;
	}
	public int[]  moveAsDijkstra(int Vstart,int Vend){
		/*@ REQUIRES: Vstart为出发点位置，Vend为目的地位置;
		@ MODIFIES: None;
		@ EFFECTS: 对于提供的Vstart和Vend进行路径查询，调用dijkstra的findByDijsktra()方法获取
    	记录前驱顶点的Spath数组，若中途地图改变，则结束该方法，记录改变时候的出租车当前位置，
    	之后通过其他方法的调用再次运行该方法，直到到达目的地
		*/
		int[] shortPath = dijkstra.findByDijsktra(weight, flow,Vstart, Vend);
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
		//不算出发地点j=i-1
		for(int j=i-1;j>=0;j--){
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(!lightAllowsMoveThen(changeToPositon(preposition), position, changeToPositon(path[j]))){
				//若需要等待红绿灯，则等待相应时间
				try {
					bw.write("该车在("+position[0]+","+position[1]+")处由于信号灯限制而停止"+System.getProperty("line.separator"));
					bw.flush();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				while(!lightAllowsMoveThen(changeToPositon(preposition), position, changeToPositon(path[j]))){
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			preposition=changeToIndex(position);
			flowThread.receiveChangeFlow(position[0]*map.length+position[1],path[j]);
			updatePosition(position, path[j]);
			Vstart=path[j];
			Point point=new Point(position[0], position[1]);
			gui.SetTaxiStatus(id, point, status);
			System.out.print("-->"+"("+position[0]+","+position[1]+")");
			PathString=PathString.concat("-->"+"("+position[0]+","+position[1]+")");
			try {
				bw.write("-->"+"("+position[0]+","+position[1]+")");
				bw.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(mapIsChanged){
				int array[]={Vstart,Vend};
				mapIsChanged=false;
				return array;
			}
		}
		System.out.println();
		try {
			bw.newLine();
			bw.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}
	//随机移动
	public void random_move(){
		/*@ REQUIRES: None;
		@ MODIFIES: this;
		@ EFFECTS: 休眠0.2s，而后出租车随机运动，兼顾可连通道路与流量最小的条件，找到下一个运动的
		点坐标，更新位置
		*/
		//判断 清零  加
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		int Sweight[]=weight[position[0]*map.length+position[1]];
		while(flow==null){
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		int Flowweight[]=flow[position[0]*map.length+position[1]];
		int canReach[]=new int[4];
		int count=0;
		for(int i=0;i<Sweight.length;i++){
			//把所有的能够连同的点都记录下来，一个点最多和4个连通，(不算自己)
			if(Sweight[i]==1){
				canReach[count++]=i;
			}
		}
		//选择流量最小的路径
		int canReachMinFlow[]=new int[4];
		int minflow=100;
		int index=0;
		for(int i=0;i<count;i++){
			if(Flowweight[canReach[i]]<minflow){
				minflow=Flowweight[canReach[i]];
			}
		}
		for(int i=0;i<count;i++){
			if(minflow==Flowweight[canReach[i]]){
				canReachMinFlow[index++]=canReach[i];
			}
		}
		Random random = new Random();
		if(index!=0){
			int rand=random.nextInt(index);
			//更新流量信息
			boolean sign=false;
			if(first==0){
				sign=lightAllowsMove(position, changeToPositon(canReach[rand]));
				preposition=changeToIndex(position);
				first++;
			}
			else{
				sign=lightAllowsMoveThen(changeToPositon(preposition),position,changeToPositon(canReach[rand]));
				preposition=changeToIndex(position);
			}
			if(!sign){
				//若需要等待红绿灯，则等待相应时间
				while(!lightAllowsMoveThen(changeToPositon(preposition),position,changeToPositon(canReach[rand]))){
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			flowThread.receiveChangeFlow(position[0]*map.length+position[1],  canReach[rand]);
			updatePosition(position, canReach[rand]);
		}
		Point point=new Point(position[0], position[1]);
		gui.SetTaxiStatus(id, point, status);
	}
	
	public void lookUpRequestListAndRandomMove(){
		/*@ REQUIRES: None;
		@ MODIFIES: this;
		@ EFFECTS: 调用random_move()方法，若某请求发出时刻该出租车可抢单，则记录到taxiRequestList列表中
		*/
		setStatus(WaitServe);
		if(requestList.isEmpty()){
			random_move();
		}
		else{
			for(int i=0;i<requestList.size();i++){
				Request request=requestList.get(i);
				//满足在4*4范围内，且满足等待服务状态可以抢单
				if(status==WaitServe && Math.abs(request.getSrc()[0]-position[0])<=2 && Math.abs(request.getSrc()[1]-position[1])<=2 && isNotInList(request,taxiRequestList)){
					addCredit(1);
					taxiRequestList.add(request);
					//request的队列添加相应的出租车
					request.add(this);
				}
			}
			random_move();
		}
	}
	public void getFlow(int flow[][]){
		/*@ REQUIRES: None;
		@ MODIFIES: this;
		@ EFFECTS: 得到流量线程中的流量数组，供出租车运动使用
		*/
		this.flow=flow;
	}
	private boolean isNotInList(Request request,List<Request> taxiRequestList){
		/*@ REQUIRES: taxiRequestList里面的指令合法，request指令合法;
		@ MODIFIES: None;
		@ EFFECTS: 对于传入的请求request进行判断，若已经在出租车的响应队列中，返回false，否则返回true。
		*/
		if(taxiRequestList.isEmpty()){
			return true;
		}
		for(int i=0;i<taxiRequestList.size();i++){
			if(taxiRequestList.get(i).equalsTo(request)){
				return false;
			}
		}
		return true;
	}
	//收到的命令就是真正要执行的，也就是抢到单
	public void receiveRequest(Request request){
		/*@ REQUIRES: None;
		@ MODIFIES: this;
		@ EFFECTS: 返回真正要执行的出租车响应的请求
		*/
		this.request=request;
	}
	public int getid() {
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS:返回出租车id 
		*/
		return id;
	}
	public synchronized int[] getPosition() {
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 同步方法，返回出租车当前位置
		*/
		return position;
	}
	public synchronized  int getStatus() {
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 同步方法，返回出租车当前状态
		*/
		return status;
	}
	public synchronized  int getCredit() {
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 同步方法，返回出租车当前信用
		*/
		return credit;
	}
	public  List<Request> getTaxiRequestList() {
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 返回出租车响应过的的请求队列
		*/
		return taxiRequestList;
	}
	@Override
	public String toString() {
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 重写出租车toString方法，供输出使用
		*/
		return "Taxi [id=" + id + ", position=" + "(" + position[0] +"," + position[1] +")" + ", status=" + status + ", credit="
				+ credit + ", request=" + request + "]";
	}
	public synchronized void setPosition(int[] position) {
		/*@ REQUIRES: None;
		@ MODIFIES: this;
		@ EFFECTS: 同步方法，设置出租车位置
		*/
		this.position = position;
	}
	public synchronized  void setStatus(int status) {
		/*@ REQUIRES: None;
		@ MODIFIES: this;
		@EFFECTS: 同步方法，设置出租车状态
		*/
		this.status = status;
	}
	public synchronized  void addCredit(int credit) {
		/*@ REQUIRES: None;
		@ MODIFIES: this;
		@ EFFECTS:同步方法，增加出租车信用
		*/
		this.credit = this.credit+credit;
	}
	public void iteratorBothDirection(){
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 为父类提供一个空的方法，子类具体实现其方法
		*/
	}
	public List<Request> getDealRequestList(){
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 返回dealRequestList出租车抢到单的列表
		*/
		return dealRequestList;
	}
}

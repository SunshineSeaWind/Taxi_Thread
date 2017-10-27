package work;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Work_taxi {
	/*@OVERVIEW:
	 *出租车系统线程类，将各个线程开启，进行出租车系统的运行
	 * */
	private static int MAX=10000;
	public boolean repOK(){
		/*@ REQUIRES: None
		@ MODIFIES: None;
		@ EFFECTS: 该类不存在构造该类对象时候的参数满足条件判断，故直接返回true
		*/
		return true;
	}
	public static void main(String[] args) {
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 调用各个线程并开启线程，对一些需要用到的共享变量进行初始化，
		得到map，weight，flow，light四个重要的数组
		*/
		try {
			//可以在此修改路径
			String mapPath="map.txt";
			String lightPath="light.txt";
			TaxiGUI gui=new TaxiGUI();
			int map[][]=new int[80][80];
			int newTaxiMap[][]=new int[80][80];
			int weight[][]=new int[6400][6400];
			int newTaxiWeight[][]=new int[6400][6400];
			int flow[][]=new int[6400][6400];
			int light[][]=new int [80][80];
			int lightControl[][]=new int[80][80];
			int idArray[]=new int [30];
			List<Request> requestList=new ArrayList<Request>();
			Taxi taxiArray[]=new Taxi[100];
			Readmap readmap=new Readmap();
			//将文本中的地图放入map以及newTaxiMap中
			readmap.getMap(map, mapPath);
			readmap.getMap(newTaxiMap, mapPath);
			//交通等控制数组light[][]
			ReadLights readLights=new ReadLights(map);
			readLights.getLight(light, lightPath);
//			readLights.setLightFile();
			gui.LoadMap(map, 80);
			//初始化权重邻接矩阵与流量数组
			for(int i=0;i<weight.length;i++){
				for(int j=0;j<weight.length;j++){
					if(i==j){
						weight[i][j]=0;
						newTaxiWeight[i][j]=0;
					}
					else{
						weight[i][j]=MAX;
						newTaxiWeight[i][j]=MAX;
					}
					flow[i][j]=0;
				}
			}	
			//构造权重数组weight
			readmap.getWeightMatrix(weight, map);
			readmap.getWeightMatrix(newTaxiWeight, newTaxiMap);
			//流量线程
			FlowThread flowThread=new FlowThread(taxiArray, flow);
			//红路灯线程
			LightControlThread lightControlThread=new LightControlThread(light, lightControl,gui);
			//出租车线程开始
			initTaxi(idArray);
			for(int i=0;i<100;i++){
				//随机生成位置,[0,6399] 80*80-1
				Random rand = new Random();
				int index=rand.nextInt(6400);
				int position[]=new int[2];
				position[0]=index/map.length;
				position[1]=index%map.length;
				if(isInArray(idArray, i)){
					//可追踪出租车
					taxiArray[i]=new NewTaxi(i, position, requestList, newTaxiWeight, newTaxiMap,gui,flowThread,light,lightControl,lightControlThread.getTimeInteval());
				}
				else{
					//普通出租车
					taxiArray[i]=new Taxi(i, position, requestList, weight, map,gui,flowThread,light,lightControl,lightControlThread.getTimeInteval());
				}
				taxiArray[i].start();
			}
			//流量线程开启
			flowThread.start();
			//红路灯线程开启
			lightControlThread.start();
			//线程处理输入请求
			DealInputRequest dealRequest=new DealInputRequest(requestList,taxiArray,weight,map,gui,newTaxiMap,newTaxiWeight);
			dealRequest.start();
			DealRequestList dealRequestList=new DealRequestList(requestList, weight, map, taxiArray,flowThread);
			dealRequestList.start();
			//休眠1s以便所有进程都准备好了相应初始化工作
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("出租车线程全部开启，请输入请求!");
			//第一个参数输入出租车id 0到99，第二个参数就传递taxiArray即可，第三个参数是代表间隔时间
			//比如可以用接口查看一号出租车,间隔10000ms即10s输出一次
//			TaxiInfo taxiInfo=new TaxiInfo(10, taxiArray,10000);
//			taxiInfo.start();
			//休眠一分钟后，对可以进行迭代的可追踪出租车进行输出，测试者可以在这里进行时间间隔的修改，也可写成循环的形式，每经过设定的
			//时间间隔就进行一次双向迭代
			Thread.sleep(60000);
			/*循环迭代，本程序默认0-29号出租车为可追踪出租车，故进行循环编写，将每一辆车都进行迭代处理，测试者
			可以在这里修改自己规定的可追踪出租车,例如直接调用iteratorTaxi(45, taxiArray,idArray)等*/
			for(int i=0;i<30;i++){
				iteratorTaxi(i, taxiArray,idArray);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	public static void initTaxi(int idArray[]){
		/*@ REQUIRES: None;
		@ MODIFIES: this;
		@ EFFECTS: 将出租车数组进行初始化，按照规定，分为可追踪出车与普通出租车
		*/
		//初始化出租车数组,可以修改tempArray的出租车id来实现任意顺序的可追踪出租车分配,此处默认0-29号出租车为可追踪出租车
		int tempArray[]={0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29};
		for(int i=0;i<30;i++){
			idArray[i]=tempArray[i];
		}
	}
	public static boolean isInArray(int idArray[],int num){
		/*@ REQUIRES:  0=<num<=99；
		@ MODIFIES: None;
		@ EFFECTS: 判断num这个可追踪出租车的id是否在idArray数组里面，若在，返回true；否则返回false
		*/
		for(int i=0;i<30;i++){
			if(idArray[i]==num){
				return true;
			}
		}
		return false;
	}
	public static void iteratorTaxi(int id,Taxi taxiArray[],int idArray[]){
		/*@ REQUIRES:  0=<id<=99 且 id对应的出租车可追踪，taxiArray为存放出租车的数组;
		@ MODIFIES: None;
		@ EFFECTS: 对可追踪出租车进行迭代处理，若能迭代，进行迭代，输出响应信息，否则输出不可迭代的提示
		*/
		if(isInArray(idArray, id)){
			if(!((taxiArray[id]).getDealRequestList().isEmpty())){
				System.out.println("可追踪出租车"+id+"的迭代情况如下：");
				taxiArray[id].iteratorBothDirection();
			}
			else{
				System.out.println("在经过设定时间后，可追踪出租车"+id+"处理过的请求为空，不可以进行迭代!");
			}
		}
		else{
			System.out.println("出租车"+id+"不是可追踪出租车，不可以进行迭代!");
		}
	}
}


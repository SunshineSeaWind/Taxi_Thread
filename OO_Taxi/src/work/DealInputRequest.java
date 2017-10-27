package work;

import java.awt.Point;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DealInputRequest extends Thread{
	/*@OVERVIEW:
	 *处理输入指令的线程类，对输入的指令的指令类型以及合理性进行判断，若
	 * 输入指令满足相应的限制条件，若为用户请求，则添加指令到相应的队列，若为
	 * 地图改变指令，则进行地图改变以及GUI的更新
	 * */
	private static int MAXDIS=10000;
	private List<Request> requestList=new ArrayList<Request>();
	private Taxi taxiArray[];
	private TaxiGUI gui;
	private int map[][];
	private int newTaxiMap[][];
	private int weight[][];
	private int newTaxiWeight[][];
	public DealInputRequest(List<Request> requestList,Taxi taxiArray[],int weight[][],int map[][],TaxiGUI gui,int newTaxiMap[][],int newTaxiWeight[][]) {
		/*@ REQUIRES: requestList为空，其余参数均合法即可;
		@ MODIFIES: this;
		@ EFFECTS: 构造函数，接收主线程传来的参数,将符合条件的请求加入requestList，并为其他数据赋值
		*/
		this.requestList=requestList;
		this.taxiArray=taxiArray;
		this.map=map;
		this.weight=weight;
		this.gui=gui;
		this.newTaxiMap=newTaxiMap;
		this.newTaxiWeight=newTaxiWeight;
	}
	public boolean repOK(){
		/*@ REQUIRES: taxiArray!=null && taxiArray.length==100
		 *  &&map!=null && weight!=null && gui!=null;
		@ MODIFIES: None;
		@ EFFECTS: 若符合类所需的参数条件，则返回true，否则返回false
		*/
		if(taxiArray==null || taxiArray.length!=100) return false;
		if(map==null) return false;
		if(weight==null) return false;
		if(gui==null) return false;
		return true;
	}
	private void DealInput(){
		/*@ REQUIRES: None;
		@ MODIFIES: this;
		@ EFFECTS: 对输入进行合理性判断，若为乘客请求，将其加入请求队列中；
		若为修改地图指令，则将地图进行修改；若输入不符合格式则报出相应错误信息
		*/
		Scanner scanner=new Scanner(System.in);
		int count=0;
		while(scanner.hasNextLine()){
			String string=scanner.nextLine();
			DecimalFormat dcmFmt = new DecimalFormat("0.0");
			double inputTime=Double.valueOf(dcmFmt.format(System.currentTimeMillis()/1000.0));
			if(string.equals("end")){
				break;
			}
			String regEx = "\\[CR,\\([+]?([0-9]|[1-7][0-9]),[+]?([0-9]|[1-7][0-9])\\),\\([+]?([0-9]|[1-7][0-9]),[+]?([0-9]|[1-7][0-9])\\)\\]";
			String regEx_map="map\\[[+]?([0-9]|[1-7][0-9])\\]\\[[+]?([0-9]|[1-7][0-9])\\]=[+]?[0-3]";
			// 编译正则表达式
			Pattern pattern = Pattern.compile(regEx);
			Pattern pattern_map = Pattern.compile(regEx_map);
			Matcher matcher = pattern.matcher(string);
			Matcher matcher_map = pattern_map.matcher(string);
			if(matcher.matches()){
				count=0;
				String [] str=string.split(",");
				int src_x=Integer.valueOf(str[1].substring(1));
				int src_y=Integer.valueOf(str[2].substring(0,str[2].length()-1));
				int src[]={src_x,src_y};
				int dst_x=Integer.valueOf(str[3].substring(1));
				int dst_y=Integer.valueOf(str[4].substring(0,str[4].length()-2));
				int dst[]={dst_x,dst_y};
				if(src_x==dst_x && src_y==dst_y){
					System.out.println("输入的出发地与目的地重复!");
					continue;
				}
				Request request=new Request("CR", src, dst, inputTime);
				if(requestList.isEmpty()){
					System.out.println("读入请求:"+request);
					for(int j=0;j<taxiArray.length;j++){
						if(Math.abs(request.getSrc()[0]-taxiArray[j].getPosition()[0])<=2 && Math.abs(request.getSrc()[1]-taxiArray[j].getPosition()[1])<=2){
							request.InregionAdd(taxiArray[j]);
						}
					}
					requestList.add(request);
				}
				else{
					for(int i=0;i<requestList.size();i++){
						if(request.equalsTo(requestList.get(i))){
							System.out.println("输入请求有重复!");
							break;
						}
						if(i==requestList.size()-1){
							System.out.println("读入请求:"+request);
							//在请求发生的时刻，找到4*4范围内所有的出租车，不管状态
							for(int j=0;j<taxiArray.length;j++){
								if(Math.abs(request.getSrc()[0]-taxiArray[j].getPosition()[0])<=2 && Math.abs(request.getSrc()[1]-taxiArray[j].getPosition()[1])<=2){
									request.InregionAdd(taxiArray[j]);
								}
							}
							requestList.add(request);
							//rerequestList的size变化了，所以i++
							i++;
						}
					}
				}
			}
			else if(matcher_map.matches()){
				if(count>=5){
					System.out.println("输入的改变地图指令条数过多!");
					continue;
				}
				int num_x,num_y,num;
				String regEx_1="[+]?[0-9]+";
				// 编译正则表达式
				Pattern pattern_1 = Pattern.compile(regEx_1);
				Matcher matcher_1 = pattern_1.matcher(string);
				matcher_1.find();
				num_x=Integer.parseInt(matcher_1.group());
				matcher_1.find();
				num_y=Integer.parseInt(matcher_1.group());
				matcher_1.find();
				num=Integer.parseInt(matcher_1.group());
				if(checkIsOK(num_x, num_y, num)){
					count++;
					//更新map与权重邻接矩阵
					DealMapChanged(num_x,num_y,num);
					map[num_x][num_y]=num;
					newTaxiMap[num_x][num_y]=newTaxiMapNum(num_x, num_y, num);
					updateWeight(num_x,num_y,num);
					updatenewTaxiWeight(num_x, num_y, num);
					//更新出租车地图状态
					for(int i=0;i<100;i++){
					taxiArray[i].receiveMapChange();
					}
				}
				else{
					System.out.println("输入的改变地图指令不符合规定!");
					continue;
				}
			}
			else{
				count=0;
				System.out.println("输入有误!");
			}
		}
	}
	private void updatenewTaxiWeight(int num_x, int num_y, int num) {
		/*@ REQUIRES:  0<=num_x<=79与0<=num_y<=79 ,num=0|1|2|3;
		@ MODIFIES: this;
		@ EFFECTS: 将修改后的地图形成相对应的权重数组，修改相应改变的weight数组数值，更新权重
		*/
		int down=num_x+1;
		int right=num_y+1;
		if(down<=79){
			if(num==3 || num==2){
				newTaxiWeight[num_x*newTaxiMap.length+num_y][down*newTaxiMap.length+num_y]=newTaxiWeight[down*newTaxiMap.length+num_y][num_x*newTaxiMap.length+num_y]=1;
			}
		}
		if(right<=79){
			if(num==3 || num==1){
				newTaxiWeight[num_x*newTaxiMap.length+num_y][num_x*newTaxiMap.length+right]=newTaxiWeight[num_x*newTaxiMap.length+right][num_x*newTaxiMap.length+num_y]=1;
			}
		}
	}
	private int newTaxiMapNum(int num_x, int num_y, int num){
		/*@ REQUIRES:  0<=num_x<=79与0<=num_y<=79 ,num=0|1|2|3;
		@ MODIFIES: None;
		@ EFFECTS: 得到新地图对于地图改变指令而应该对自己的地图进行的正确赋值，返回针对不同输入而产生的不同地图赋值结果
			若num==0，返回flag；若num==1：若 flag==0 || flag==1 返回1；若 flag==2 || flag==3 返回3；
			若num==2：若flag==0 || flag==2 返回2；若flag==1 || flag==3 返回3；若 num==3，返回3；若均不满足，返回-1。
		*/
		int flag=newTaxiMap[num_x][num_y];
		if(num==0){
			return flag;
		}
		else if(num==1){
			if(flag==0 || flag==1) return 1;
			if(flag==2 || flag==3) return 3;
		}
		else if(num==2){
			if(flag==0 || flag==2) return 2;
			if(flag==1 || flag==3) return 3;
		}
		else if(num==3){
			return 3;
		}
		return -1;
	}
	private void DealMapChanged(int num_x, int num_y, int num) {
		/*@ REQUIRES: 0<=num_x<=79与0<=num_y<=79 ,num=0|1|2|3;
		@ MODIFIES: this;
		@ EFFECTS:将输入的改变地图指令实时更新到gui中，以便gui上的地图显示能够与实际输入情况同步
		*/
		int down=num_x+1;
		int right=num_y+1;
		if(right<=79){
			Point p1=new Point(num_x,num_y);
			Point p2=new Point(num_x,right);
			//先都清除
			gui.SetRoadStatus(p1,p2,0);
			if(num==3 || num==1){
				gui.SetRoadStatus(p1,p2,1);
			}
		}
		if(down<=79){
			Point p1=new Point(num_x,num_y);
			Point p2=new Point(down,num_y);
			gui.SetRoadStatus(p1,p2,0);
			if(num==3 || num==2){
				gui.SetRoadStatus(p1,p2,1);
			}
		}
	}
	@Override
	public void run() {
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 调用DealInput()方法，时刻进行着输入线程的运行
		*/
		try {
			DealInput();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	private boolean checkIsOK(int num_x,int num_y,int num){
		/*@ REQUIRES: 0<=num_x<=79与0<=num_y<=79 ,num=0|1|2|3;
		@ MODIFIES: None;
		@ EFFECTS: 对输入的num_x,num_y,num进行合理性判断，若合理返回true，
		否则返回false；
		*/
		if(num_x>=0 && num_x<=78 && num_y>=0 && num_y<=78){
			return true;
		}
		if(num_x==79 && num_y>=0 && num_y<=78 && (num==1 || num==0)){
			return true;
		}
		if(num_y==79 && num_x>=0 && num_x<=78 && (num==2 || num==0)){
			return true;
		}
		if(num_x==79 && num_y==79 && num==0){
			return true;
		}
		return false;
	}
	private void updateWeight(int num_x,int num_y,int num){
		/*@ REQUIRES:  0<=num_x<=79与0<=num_y<=79 ,num=0|1|2|3;
		@ MODIFIES: this;
		@ EFFECTS: 将修改后的地图形成相对应的权重数组，修改相应改变的weight数组数值，并且更新权重
		*/
		int down=num_x+1;
		int right=num_y+1;
		if(down<=79){
			weight[num_x*map.length+num_y][down*map.length+num_y]=weight[down*map.length+num_y][num_x*map.length+num_y]=MAXDIS;
			if(num==3 || num==2){
				weight[num_x*map.length+num_y][down*map.length+num_y]=weight[down*map.length+num_y][num_x*map.length+num_y]=1;
			}
		}
		if(right<=79){
			weight[num_x*map.length+num_y][num_x*map.length+right]=weight[num_x*map.length+right][num_x*map.length+num_y]=MAXDIS;
			if(num==3 || num==1){
				weight[num_x*map.length+num_y][num_x*map.length+right]=weight[num_x*map.length+right][num_x*map.length+num_y]=1;
			}
		}
		}
}

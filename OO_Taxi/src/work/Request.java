package work;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Request {
	/*@OVERVIEW:
	 *对用户请求进行Request类的建立，构造方法通过传入相应的参数为每一个
	 *用户请求建立相应的Request类，同时包含多个方法，如添加满足条件的出租车
	 *到队列，移出出租车等操作。
	 * */
	private String sign;
	private int src[]=new int [2];
	private int dst[]=new int [2];
	private double requestTime;
	private List<Taxi> requestTaxiList=new ArrayList<Taxi>();//抢单时间内，所有抢单的出租车
	private List<String> requestInregionTaxiList=new ArrayList<String>();//请求发出时，处于以请求src为中心的4×4区域中的所有出租车
	public Request(String sign, int[] src, int[] dst, double requestTime) {
		/*@ REQUIRES: None;
		@ MODIFIES: this;
		@ EFFECTS: 构造方法，为请求传递参数，最终生成该请求
		*/
		this.sign = sign;
		this.src = src;
		this.dst = dst;
		this.requestTime = requestTime;
	}
	public boolean repOK(){
		/*@ REQUIRES: sign==CR && 0<=src[0]<=79 && 0<=src[1]<=79 &&
		 * 0<=dst[0]<=79 && 0<=dst[1]<=79  && requestTime>=0
		@ MODIFIES: None;
		@ EFFECTS: 若符合类所需的参数条件，则返回true，否则返回false
		*/
		if(!sign.equals("CR"))  return false;
		if(src[0]<0 || src[0]>79 || src[1]<0 || src[1]>79) return false;
		if(dst[0]<0 || dst[0]>79 || dst[1]<0 || dst[1]>79) return false;
		if(requestTime<0) return false;
		return true;
	}
	public boolean equalsTo(Request request){
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 判断两个指令是否完全相同，若相同，返回true；否则返回false；
		*/
		if(this.sign.equals(request.getSign()) && this.src[0]==request.getSrc()[0] && this.src[1]==request.getSrc()[1] &&
		this.dst[0]==request.getDst()[0] && this.dst[1]==request.getDst()[1] && this.requestTime==request.getRequestTime()){
			return true;
		}
		return false;
	}
	public String getSign() {
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 返回请求的标识符
		*/
		return sign;
	}
	public int[] getSrc() {
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 返回出发点坐标
		*/
		return src;
	}
	public int[] getDst() {
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 返回目的地坐标
		*/
		return dst;
	}
	public double getRequestTime() {
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 返回请求时间
		*/
		return requestTime;
	}
	@Override
	public String toString() {
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS:重写toString方法，提供请求命令的信息表示格式
		*/ 
		return "Request [sign=" + sign + ", src=" + "(" + src[0] +"," + src[1] +")" + ", dst=" + "(" + dst[0] +"," + dst[1] +")"
				+ ", requestTime=" + new DecimalFormat("0.0").format(requestTime) + "]";
	}
	public synchronized boolean add(Taxi taxi){
		/*@ REQUIRES: None;
		@ MODIFIES: this;
		@ EFFECTS: 同步方法，为该请求具有的可响应的出租车队列添加相应出租车
		*/
		return requestTaxiList.add(taxi);
	}
	public synchronized boolean remove(Taxi taxi){
		/*@ REQUIRES: None;
		@ MODIFIES: this;
		@ EFFECTS: 同步方法，为该请求具有的可响应的出租车队列删除相应出租车
		*/
		return requestTaxiList.remove(taxi);
	}
	public synchronized boolean InregionAdd(Taxi taxi){
		/*@ REQUIRES: None;
		@ MODIFIES: this;
		@ EFFECTS: 同步方法，为该请求具有的请求发生时刻可响应的出租车队列添加相应出租车
		*/
		return requestInregionTaxiList.add(taxi.toString());
	}
	public synchronized boolean InregionRemove(Taxi taxi){
		/*@ REQUIRES: None;
		@ MODIFIES: this;
		@ EFFECTS: 同步方法，为该请求具有的请求发生时刻可响应的出租车队列删除相应出租车
		*/
		return requestInregionTaxiList.remove(taxi.toString());
	}
	public List<Taxi> getRequestTaxiList(){
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 返回该请求具有的所有可响应的出租车队列
		*/
		return requestTaxiList;
	}
	public List<String> getRequestInregionTaxiList() {
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 返回该请求具有的请求发生时刻可响应的出租车队列
		*/
		return requestInregionTaxiList;
	}
	
}

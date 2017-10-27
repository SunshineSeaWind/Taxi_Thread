package work;

public class TaxiInfo extends Thread{
	/*@OVERVIEW:
	 *出租车信息显示线程类，通过传入的出租车id以及时间间隔，在经过相应的时间间隔后
	 *就对出租车的相关信息在控制台进行打印
	 * */
	private Taxi taxiArray[];
	private int id;
	private Taxi taxi;
	private int ms;
	public TaxiInfo(int id,Taxi taxiArray[],int ms) {
		/*@ REQUIRES: 0<=id<=99;
		@ MODIFIES: this;
		@ EFFECTS: 构造方法，传递出租车信息显示线程的需要参数，初始化数据
		*/
		if(id>=100 || id<0){
			System.out.println("检测线程输入id有误!");
			System.exit(0);
		}
		this.id=id;
		this.taxiArray=taxiArray;
		this.taxi=taxiArray[id];
		this.ms=ms;
	}
	public boolean repOK(){
		/*@ REQUIRES:  0<=id<=99  && taxiArray!=null && taxiArray.length==100
		@ MODIFIES: None;
		@ EFFECTS: 若符合类所需的参数条件，则返回true，否则返回false
		*/
		if(taxiArray==null || taxiArray.length!=100) return false;
		if(id>=100 || id<0) return false;
		return true;
	}
	@Override
	public void run() {
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 休眠测试者设置的休眠时间后，对出租车相关信息进行打印输出
		*/
		try {
			while(true){
				try {
					try {
						Thread.sleep(ms);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println("接口输出信息:间隔"+ms+"ms   taxi "+id+" status="+taxi.getStatus()+" position=("+taxi.getPosition()[0]+
							","+taxi.getPosition()[1]+")   "+"credit = "+taxi.getCredit());
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
	}
	public Taxi getTaxiInfo(){
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 返回需要观察的出租车变量
		*/
		return taxi;
	}
}

package work;

import java.awt.Point;
import java.util.Random;

public class LightControlThread extends Thread{
	/*@OVERVIEW:
	 *信号灯控制线程类，将传入的light控制信号灯数组进行读取并且根据这个数组
	 *随机生成lightControl数组中的信号灯类型，信号灯类型每经过timeInteval时间间隔
	 *进行变化，为后续出租车运动提供判断条件
	 * */
	private static final int EWGREAN=1;
	private static final int SNGREAN=2;
	private int light[][];
	private int lightControl[][];
	private int timeInteval=0;
	private TaxiGUI gui;
	public LightControlThread(int light[][],int lightControl[][],TaxiGUI gui) {
		/*@ REQUIRES: None;
		@ MODIFIES:this;
		@ EFFECTS: 构造函数，接收主线程传来的参数，并且调用信号灯的初始化方法
		*/
		this.light=light;
		this.lightControl=lightControl;
		this.gui=gui;
		initLight();
	}
	public boolean repOK(){
		/*@ REQUIRES: light!=null && gui!=null
		@ MODIFIES: None;
		@ EFFECTS: 若符合类所需的参数条件，则返回true，否则返回false
		*/
		if(light==null ) return false;
		if(gui==null ) return false;
		return true;
	}
	public int getTimeInteval() {
		/*@ REQUIRES: None;
		@ MODIFIES:None;
		@ EFFECTS: 返回该线程timeInteval变量
		*/
		return timeInteval;
	}
	@Override
	public void run() {
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 首先随机产生一个信号灯变化时间间隔，初始化lightControl数组，之后每过timeInteval时间间隔，
		就将 lightControl控制的信号灯进行信号灯类型的转换
		*/
		try {
			readLight();
			while(true){
				setGUILight();
				try {
					Thread.sleep(timeInteval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				changeLight();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	private void setGUILight() {
		/*@ REQUIRES: None;
		@ MODIFIES: this;
		@ EFFECTS: 根据lightControl数组来对gui的红绿灯进行更新，使gui的显示实时更新
		*/
		for(int i=0;i<lightControl.length;i++){
			for(int j=0;j<lightControl.length;j++){
				gui.SetLightStatus(new Point(i,j), lightControl[i][j]);
			}
		}
	}
	private void changeLight() {
		/*@ REQUIRES: None;
		@ MODIFIES: this;
		@ EFFECTS: 同步方法，保证信号灯数组的改变为原子操作。每次经过相应时间间隔后，将信号灯控制数组中的信号灯类型进行转换。
		*/
		synchronized (lightControl) {
			for(int i=0;i<lightControl.length;i++){
				for(int j=0;j<lightControl.length;j++){
					int temp=lightControl[i][j];
					if(temp==EWGREAN){
						lightControl[i][j]=SNGREAN;
					}
					else if(temp==SNGREAN){
						lightControl[i][j]=EWGREAN;
					}
					else{
						;
					}
				}
			}
		}
	}
	private void initLight(){
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 随机初始化信号灯改变间隔时间200ms—500ms
		*/
		Random random = new Random();
		timeInteval=random.nextInt(301)+200;
	}
	private void readLight(){
		/*@ REQUIRES: None;
		@ MODIFIES: None;
		@ EFFECTS: 若道口被信号灯控制，则随机初始化该道口的信号灯显示；若道口没有被信号灯控制
		设置lightControl数组对应位置为0，表示无信号灯；若均不符合，则输出提示错误信息。
		*/
		for(int i=0;i<light.length;i++){
			for(int j=0;j<light.length;j++){
				int temp=light[i][j];
				if(temp==0){
					lightControl[i][j]=0;
				}
				else if(temp==1){
					Random random = new Random();
					int num=random.nextInt(2)+1;//1 or 2 
					lightControl[i][j]=num;
				}
				else{
					System.out.println("输入控制信号灯文件有误!");
					System.exit(0);
				}
			}
		}
	}
}

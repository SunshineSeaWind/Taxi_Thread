package work;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadLights {
	/*@OVERVIEW:
	 *将信号灯控制文件读入，结合map数组生对文件的合理性进行判断，若
	 *文件符合条件，则生成相应的light数组
	 * */
	private int count=0;
	private String regEx_1= "(\\s*[01]){80}\\s*";
	private String regEx_2= "\\s*0(\\s*[01]){78}\\s*0\\s*";//第一行与最后一行
	private Pattern pattern_1 = Pattern.compile(regEx_1);
	private Pattern pattern_2 = Pattern.compile(regEx_2);
	private int map[][];
	public ReadLights(int map[][]) {
		/*@ REQUIRES: None;
		@ MODIFIES:this;
		@ EFFECTS: 构造函数，接收主线程传来的参数，进行map数组的设置
		*/
		this.map=map;
	}
	public boolean repOK(){
		/*@ REQUIRES: map!=null
		@ MODIFIES: None;
		@ EFFECTS: 若符合类所需的参数条件，则返回true，否则返回false
		*/
		if(map==null ) return false;
		return true;
	}
	public void getLight(int light[][],String filePath){
		/*@ REQUIRES: filePath要为正确的信号灯控制文件路径;
		@ MODIFIES: this;
		@ EFFECTS: 通过输入的文件路径，为light设置正确的数值；若light的格式不正确，
		则输出相关的错误提醒信息。
		*/
		File file=new File(filePath);
		if(!file.exists()){
			System.out.println("输入的信号灯控制文件路径不正确!");
			System.exit(0);
		}
		try {
			FileInputStream inputstream = new FileInputStream(filePath); 
			String line; // 用来保存每行读取的内容 
			BufferedReader bufferreader = new BufferedReader(new InputStreamReader(inputstream)); 
			try {
				line = bufferreader.readLine();// 读取第一行 
				if(line==null){
					System.out.println("文件为空!");
					System.exit(0);
				}
				while (line != null && count<80) { // 如果 line 为空说明读完了 
					int index=0;
					if(count==0 || count==79){
						Matcher matcher = pattern_2.matcher(line);
						if(matcher.matches()){
							for(int i=0;i<line.length();i++){
								char c=line.charAt(i);
								if(Character.isDigit(c)){
									light[count][index]=Integer.parseInt(c+"");
									index++;
								}
							}
						}
						else{
							System.out.println("信号灯控制文件输入有误!");
							System.exit(0);
						}
					}
					else if(count<79){
						Matcher matcher = pattern_1.matcher(line);
						if(matcher.matches()){
							for(int i=0;i<line.length();i++){
								char c=line.charAt(i);
								if(Character.isDigit(c)){
									light[count][index]=Integer.parseInt(c+"");
									index++;
								}
							}
						}
						else{
							System.out.println("信号灯控制文件输入有误!");
							System.exit(0);
						}
					}
					count++;
					line = bufferreader.readLine(); // 读取下一行 
					} 
				if(line!=null){
					System.out.println("信号灯控制文件输入大于80行!");
					System.exit(0);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if(!judgeLight(light)){
			System.out.println("信号灯控制文件有误，某些红绿灯被设置在了非丁字而且非十字路口!");
			System.exit(0);
		}
}
	private boolean judgeLight(int light[][]){
		/*@ REQUIRES: None;
		@ MODIFIES:None;
		@ EFFECTS: 对于light文件的合理性进行判断，若在非十字路口且非丁字路口设置了红绿灯，即认为light文件不合理，返回false；
		否则返回true；
		*/
		for(int i=0;i<light.length;i++){
			for(int j=0;j<light.length;j++){
				if(light[i][j]==1){
					int canReach=0;
					int up=i-1;
					int down=i+1;
					int left=j-1;
					int right=j+1;
					if(up>=0){
						if(map[up][j]!=2 && map[up][j]!=3){
							;
						}
						else{
							canReach++;
						}
					}
					if(down<=79){
						if(map[i][j]!=2 && map[i][j]!=3){
							;
						}
						else{
							canReach++;
						}
					}
					if(left>=0){
						if(map[i][left]!=1 && map[i][left]!=3){
							;
						}
						else{
							canReach++;
						}
					}
					if(right<=79){
						if(map[i][j]!=1 && map[i][j]!=3){
							;
						}
						else{
							canReach++;
						}
					}
					if(canReach<3){
						return false;
					}
				}
			}
		}
		return true;
	}
	public void setLightFile() throws IOException{
		/*@ REQUIRES: None;
		@ MODIFIES:None;
		@ EFFECTS: 对于某个map文件，为其所有的十字路口或者丁字路口设置了红绿灯控制按钮，最后将生成文件导出，
		方便测试者进行红绿灯的测试
		*/
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter("C:/Users/Administrator/Desktop/lightmap.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(int i=0;i<map.length;i++){
			for(int j=0;j<map.length;j++){
					int canReach=0;
					int up=i-1;
					int down=i+1;
					int left=j-1;
					int right=j+1;
					if(up>=0){
						if(map[up][j]!=2 && map[up][j]!=3){
							;
						}
						else{
							canReach++;
						}
					}
					if(down<=79){
						if(map[i][j]!=2 && map[i][j]!=3){
							;
						}
						else{
							canReach++;
						}
					}
					if(left>=0){
						if(map[i][left]!=1 && map[i][left]!=3){
							;
						}
						else{
							canReach++;
						}
					}
					if(right<=79){
						if(map[i][j]!=1 && map[i][j]!=3){
							;
						}
						else{
							canReach++;
						}
					}
					if(canReach>=3){
						bw.write("1");
						bw.flush();
					}
					else{
						bw.write("0");
						bw.flush();
					}
			}
			bw.write(System.getProperty("line.separator"));
			bw.flush();
		}
	}
}
	
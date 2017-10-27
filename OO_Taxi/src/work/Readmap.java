package work;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Readmap{
	/*@OVERVIEW:
	 *将地图文件读入，对地图文件的合理性进行判断，若文件符合条件，
	 *则生成相应的map数组，同时提供为map数组生成相应weight的权重数组
	 * */
	private int count=0;
	private String regEx_1= "(\\s*[0-3]){79}\\s*[02]\\s*";
	private String regEx_2= "(\\s*[01]){79}\\s*0\\s*";
	private Pattern pattern_1 = Pattern.compile(regEx_1);
	private Pattern pattern_2 = Pattern.compile(regEx_2);
	public boolean repOK(){
		/*@ REQUIRES: None
		@ MODIFIES: None;
		@ EFFECTS: 该类不存在构造该类对象时候的参数满足条件判断，故直接返回true
		*/
		return true;
	}
	public void getMap(int map[][],String filePath){
		/*@ REQUIRES: filePath要为正确的地图路径;
		@ MODIFIES: this;
		@ EFFECTS: 通过输入的文件路径，为map设置正确的数值；若map的格式不正确，
		则输出相关的错误提醒信息。
		*/
		File file=new File(filePath);
		if(!file.exists()){
			System.out.println("输入的地图路径不正确!");
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
					if(count<79){
						Matcher matcher = pattern_1.matcher(line);
						if(matcher.matches()){
							for(int i=0;i<line.length();i++){
								char c=line.charAt(i);
								if(Character.isDigit(c)){
									map[count][index]=Integer.parseInt(c+"");
									index++;
								}
							}
						}
						else{
							System.out.println("地图输入有误!");
							System.exit(0);
						}
					}
					else if(count==79){
						Matcher matcher = pattern_2.matcher(line);
						if(matcher.matches()){
							for(int i=0;i<line.length();i++){
								char c=line.charAt(i);
								if(Character.isDigit(c)){
									map[count][index]=Integer.parseInt(c+"");
									index++;
								}
							}
						}
						else{
							System.out.println("地图输入有误!");
							System.exit(0);
						}
					}
					count++;
					line = bufferreader.readLine(); // 读取下一行 
					} 
				if(line!=null){
					System.out.println("地图输入大于80行!");
					System.exit(0);
				}
				count=0;
			} catch (IOException e) {
				e.printStackTrace();
			} 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	public void getWeightMatrix(int weight[][],int map[][]){
		/*@ REQUIRES: None;
		@ MODIFIES: this;
		@ EFFECTS: 根据读入的map地图，设置权重数组weight
		*/
		for(int i=0;i<map.length;i++){
			for(int j=0;j<map.length;j++){
				weight[i*map.length+j][i*map.length+j]=0;
				int down=i+1;
				int right=j+1;
				if(down<=79){
					if(map[i][j]==2 || map[i][j]==3){
						weight[i*map.length+j][down*map.length+j]=weight[down*map.length+j][i*map.length+j]=1;
					}
				}
				if(right<=79){
					if(map[i][j]==1 || map[i][j]==3){
						weight[i*map.length+j][i*map.length+right]=weight[i*map.length+right][i*map.length+j]=1;
					}
				}
			}
		}
	}
}
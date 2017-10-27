package work;

public class Dijkstra {
	/*@OVERVIEW:
	 *结合道路流量的判断条件，查找出租车从自身地点到目的地的最短路径，提供相应的findByDijsktra查找方法，
	 *该方法在找到最短路径后，将前驱结点数组进行返回
	 * */
	public boolean repOK(){
		/*@ REQUIRES: None
		@ MODIFIES: None;
		@ EFFECTS: 该类不存在构造该类对象时候的参数满足条件判断，故直接返回true
		*/
		return true;
	}
    public int[] findByDijsktra(int[][] weight,int [][]flow,int Vstart,int Vend){
    	/*@ REQUIRES: 权重数组与流量数组要合法，Vstart为要进行运动时的出发点对应的位置，Vend为要进行运动时的终点对应的位置;
    	@ MODIFIES: None;
    	@ EFFECTS: 对于提供的Vstart和Vend进行路径查询，若找到要到达的终点，则方法停止进行返回，否则一直寻找直到找到要到达的终点，
    	方法返回的是记录前驱顶点的Spath数组
    	*/
    	int minweight;
    	int minflow;
    	int v=-1;
    	int n = weight.length; 
    	int Visited[]=new int[n];
    	int Sweight[]=new int[n];
    	int Sflow[]=new int[n];
    	int Spath[]=new int[n];//记录前驱节点
    	//初始化tempflow数组，因为之后的流量变换操作不能影响到原flow数组的数值
    	//初始化数组Sweight和Spath
    	for(int i=0;i<n;i++)
    	{
    		Sweight[i]=weight[Vstart][i];
    		Sflow[i]=flow[Vstart][i];
    		Spath[i]=Vstart;
    	}
    	//初始化Sflow,对不相连的边加以一定变换
    	for(int i=0;i<n;i++)
    	{
    		if(Sweight[i]!=0 && Sweight[i]!=1){
    			Sflow[i]=100;
    		}
    	}
    	Sweight[Vstart]=0;  
    	Visited[Vstart]=1;
    	for(int i=0;i<n-1;i++) {//循环n-1次找出所有节点的最短路径
    		minweight=Integer.MAX_VALUE;
    		minflow=100;
    		int array[]=new int[n];
    		int index=0;
    		//找到未标记的最小权重值顶点,可能有多个
    		for(int j=0;j<n;j++){
    			if(Visited[j]==0 && (Sweight[j]<minweight)){
    				minweight=Sweight[j];
    			}
    		}
    		for(int j=0;j<n;j++){
    			if(Sweight[j]==minweight){
    				array[index++]=j;
    			}
    		}
    		// 找到流量最小的边
    		for(int j=0;j<index;j++){
    			if(Visited[array[j]]==0 && Sflow[array[j]]<minflow){
    				v=array[j];
    				minflow=Sflow[array[j]];
    			}
    		}
    			Visited[v]=1;
    			if (v==Vend){
    				return Spath;
    			}
    			for(int j=0;j<n;j++){
    				//找到未标记顶点且其权值大于(v的权值+(v,j)的权值)，更新其权值
    				if(Visited[j]==0 && (minweight+weight[v][j]<Sweight[j])){
    					Sweight[j] = minweight + weight[v][j];
    					if(minflow+flow[v][j]<Sflow[j]){
    						Sflow[j]=minflow+flow[v][j];
    					}
    					Spath[j]=v; //记录前驱顶点
    				}
    			}
    	} 
    	return Spath;
    }  
}  

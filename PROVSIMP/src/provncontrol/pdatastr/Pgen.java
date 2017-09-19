package provncontrol.pdatastr;

 
import java.util.HashSet;

public class Pgen implements Ppath{
	private String a1=null;
	private String a2=null;
	private HashSet<String> entity;
	private String p;
	private HashSet<String> agent;
	private String time;
	private int tid;
	private String arg;
	private String core;
	private String title;
	private String hint=null;
	private String prem1;
	private String prem2;
 
	private String id;
	private String root;

	

	
	public Pgen(String a1,String a2, String p, String root,String id,int t) {
		this.a1=a1;
		this.a2=a2;
		this.p=p;
		entity=new HashSet();
		 
		agent=new HashSet();
		this.root=root;
		 this.id=id;
		 this.tid=t;
		 
	}
	 	public Pgen(String a1,  String p, String root,String id,int t) {
		this.a1=a1;
		entity=new HashSet();
		this.p=p;
		agent=new HashSet();
		this.root=root;
		this.id=id;
		this.tid=t;

	}
	 	
			 
		public String getRoot() {
			return root;
		}
		public String getArg() {
			return arg;
		}
	 
		public String getId() {
			return id;
		}
		public String getCore() {
			return core;
		}
		public String getTitle() {
			return title;
		}
		public String getHint() {
			return hint;
		}
	 
		public String getPrem1() {
			return prem1;
		}
		public String getPrem2() {
			return prem2;
		}
	 
	
	public String getA1() {
		return a1;
	}
	public void setA1(String a1) {
		this.a1 = a1;
	}
	public String getA2() {
		return a2;
	}
	public void setA2(String a2) {
		this.a2 = a2;
	}
	public String getP() {
		return p;
	}
	public void setP(String p) {
		this.p = p;
	}
	public HashSet getAgent() {
		return agent;
	}
	public void addAgent(String agent) {
		this.agent.add(agent);
	}
	public HashSet getEntities() {
		return entity;
	}
	public void addEntity(String ent) {
		if(!ent.equals(a2))
		this.entity.add(ent);
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}

	public int getTid() {
		return tid;
	}
	public void setTid(int tid) {
		this.tid = tid;
	}
	
	public void setForExport(){
		LabelConstructor lcon=new LabelConstructor();
		title="P"+tid+" - Generation Pattern";
		core="";
		arg="";
		hint= p ;
		String arg1,arg2,arg3,arg4,arg5,arg6,arg7,arg8;
		
		arg1=" Given the provenance chain, information "+root+" ";
		if(a2!=null){
			arg2="\n- was derived from "+a2;
			core=arg2;
		}
		if(!agent.isEmpty()){
			arg3="";
			//System.out.println("Agents"+agent);
			for(String item:agent){
				arg3+=", "+item;
			}
			arg3=arg3.substring(2, arg3.length());
			arg3="\n- was associated with "+arg3;
			core=core+arg3;
		}
		String process=lcon.getLabActivityRules(p);
		arg4="\n- was generated by "+process;
		core=core+arg4;
		if(time!=null){
			arg5="\n- was generated at time "+time;
			core=core+arg5;
		}
		if(!entity.isEmpty()){
			arg6="";
			for(String item:entity){
				arg6+=", "+item;
			}
			arg6=arg6.substring(2, arg6.length());
			arg6="\n- was generated by using "+arg6;
			core=core+arg6;
		}
	
		arg7="the stated provenance elements infer information "+root+"";
		arg8="=> Therefore, information "+root+" is credible";
		
		arg=arg1+core+"\n"+arg7+"\n"+arg8;
		
		prem1=""+root+" is a credible information";
	
		 
	//	core=core.replaceAll("\\r\\n|\\r|\\n", " ");
		prem2="Info "+core;
		
	//	System.out.println("\n\n"+hint+"\n"+title+"\n"+arg+"\n\n"+prem1+"\n"+prem2);
	//	System.out.println(arg.length());
	}
 

}
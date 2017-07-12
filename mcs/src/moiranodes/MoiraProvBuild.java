/******************************************************************************
 * This research was sponsored by the U.S. Army Research Laboratory and the
 * U.K. Ministry of Defence under the Biennial Program Plane 2013 (BPP13),
 * Project 6, Task 3: Collaborative Intelligence Analysis.
 * The U.S. and U.K. Governments are authorized to reproduce and distribute
 * reprints for Government purposes notwithstanding any copyright notation
 * hereon.
 * **************************************************************************
 * 
 * 
 * @author      Alice Toniolo  
 * @version     1.0  
 * @since 		August 2015           
 *   
 *    
 */


package moiranodes;



 


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;


import utils.JsonHelper;
import utils.NS;
import utils.PatternBuilder;
import utils.TimeExtender;

import com.hp.hpl.jena.graph.TripleBoundary;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelExtract;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StatementTripleBoundary;


 


public class MoiraProvBuild {
	//only named model

	private PatternBuilder prov;
	private TimeExtender time;
	private ModelConnect conn;
 
	private String curanaly;
	private String moira_name;
 

	public MoiraProvBuild(String mn) {
		time=new TimeExtender();
		conn=new ModelConnect();
		moira_name=mn;
	}
	


public void addNewAnalysis(ArrayList usrs) {
	 curanaly=UUID.randomUUID().toString();
	String analysis=moira_name+"Analysis"+curanaly;
	PatternBuilder prov = new PatternBuilder();
	 time=new TimeExtender();
	ArrayList users=new ArrayList();
	users.addAll(usrs);
	prov.makeSimpleAnalysisGenerationPattern(moira_name+"_analysis",analysis,curanaly,users, "MakeAnalysis"+curanaly, time.now(), new ArrayList());
	prov.addExtraPropProperty(NS.URICISP+analysis, NS.CISP+"marker",NS.CISP+"Last" );
	conn.UpdateModel(prov);
}
 


	
 

 
 
 

 

private Model extractModel(String nodeID, Model defModel) {
	StatementTripleBoundary sb = new StatementTripleBoundary(TripleBoundary.stopNowhere);
	ModelExtract md=new ModelExtract(sb);
	Resource r=defModel.getResource(nodeID);
	Model me=md.extract(r, defModel);                
	return me;

}

 


public void printAll(String user){
	Model pluto=conn.getModel();
	 System.out.println("NAMED "+user);
	 pluto.write(System.out,"N-TRIPLES");
	 System.out.println("DEFAULT ");
	 Model pippo=conn.doQueryDefaultModel();
	 pippo.write(System.out,"N-TRIPLES");
	 System.out.println(" ");
}







public HashMap getSerialisedMProv() {
	String outline=conn.writeToJson();
	JsonHelper jsh=new JsonHelper();
	HashMap newjson=jsh.convertInputMap(outline);
	// Model pippo=conn.doQueryDefaultModel();
	// pippo.write(System.out,"N-TRIPLES");
	return newjson;
}






public void addDefaultPattern(String source_name, String[] sources, String dtg) {
		prov = new PatternBuilder();
		String aid=UUID.randomUUID().toString();
		String timex=time.getDateCIS(dtg);
		for(String source:sources){
			prov.makeGenerationPattern(source_name, source, moira_name+"_Tells_"+aid, moira_name,timex);
		}
		conn.UpdateModel(prov);
}



public void addNodePatterns(ArrayList ndprov, String timex, ArrayList  uss,String source_name) {
	prov = new PatternBuilder();
	String aid=UUID.randomUUID().toString();
	HashMap node;
	ArrayList nodes=new ArrayList();
	Iterator iter=ndprov.iterator();
	 timex=time.getDateCIS(timex);
	while(iter.hasNext()){
		node=(HashMap) iter.next();
		String[] nodeID=new String[2];
		nodeID[0]=(String) node.get("nodeID");
		nodeID[1]=(String) node.get("text");
		nodes.add(nodeID[0]);
		Iterator itt=uss.iterator();
		while(itt.hasNext()){
			String user=(String)itt.next();
			user=user.replaceAll(" ", "_");
			prov.makeGenerationPattern(nodeID, source_name, "Ask_"+moira_name+aid, user, timex);
		}
		prov.addNodesToGeneration("MakeAnalysis"+curanaly, nodes);
	}
	
	conn.UpdateModel(prov);
}


	public void addNodesToAnalysis(String node){
		prov = new PatternBuilder();
		ArrayList list=new ArrayList();
		list.add(node);
		prov.addNodesToGeneration("MakeAnalysis"+curanaly, list);
		conn.UpdateModel(prov);
	}



	public void setModelProv(Model model) {
		conn.UpdateModel(model);
		
	}



	public void repeatPattern(String top_prov, ArrayList<String> ndprov, HashMap  nodemap) {
 
		//set all was derived from 
		 String query=NS.PREFIXES+ " SELECT DISTINCT ?ent WHERE{"+
				 		"?ent a prov:Entity ."+ 
				 		"<"+top_prov+"> prov:wasDerivedFrom ?ent ."+
				 		"}";
	 // System.out.println(query);

		 ArrayList der_array=conn.executeQLSingle(query); 
		// System.out.println(der_array);
		  query=NS.PREFIXES+" SELECT DISTINCT ?p ?t WHERE{"+
			 		"?p a prov:Activity ."+ 
			 		"<"+top_prov+"> prov:wasGeneratedBy ?p ."+
			 		"<"+top_prov+"> prov:qualifiedGeneration ?x ."+
					"?x prov:atTime ?t." +
			 		"}";
		//  System.out.println(query);

		 ArrayList gen_array=conn.executeQLMany(query);
		// System.out.println(gen_array);
		 
		 for(String nodeid:ndprov){
			 // node id
			 // find text
			 String text=null;
			 HashMap nodeobj=(HashMap) nodemap.get(nodeid);
			
			 Object ij;
			 if(nodeobj!=null){
				 text=(String) nodeobj.get("text");
				 String[] node=new String[]{nodeid,text};
				 ij=node;
			 }else{
				 ij=nodeid;
			 }
			 //derivation
			 Iterator iter=der_array.iterator();
			 while(iter.hasNext()){
				 String derivent=(String) iter.next();
				 Resource aa1=prov.getCISEntity(ij);
				 Resource aa2=prov.getCISEntity(derivent.replace(NS.URICISP,""));
				 prov.addWasDerivedFrom(aa1, aa2);
			 }
			 //generation
		     iter=gen_array.iterator();
			 while(iter.hasNext()){
				 ArrayList array=(ArrayList) iter.next();
				 //p then t
				 if(array.size()==2){
					 String act=(String) array.get(0);
					 String time=(String) array.get(1);
					 TimeExtender timex=new TimeExtender();
				//	 System.out.println(time);
					 time=timex.parseAndTurn(time);
					 Resource aa1,pp1;
					   //this already checks for existing ones!Brilliant!!!
						//make different entities/actors
					 aa1=prov.getCISEntity(ij);
					 prov.createActivity(act.replace(NS.URICISP,""));
					 pp1=prov.getResource(act);
						//need to add 4 relationships 
					 prov.addWasGeneratedBy(aa1, pp1,time);
				 }
			 }
			 
			 
			 
			 
			 
			 
		 
		 
		 }
		 
		 
		 
		 conn.UpdateModel(prov); 
	}
}
/*
 {"nodes":[{"input":"CLAIM","eval":"N/A","dtg":"2015/03/03 18:21:45","commit":"N/A","text":"It the case that Form 2 support","source":"","type":"I","annot":{"conc":{},"prem_assump":{}},"nodeID":"619d208d-c36e-402a-899c-8f83f0fdbef8","uncert":"Confirmed"},{"input":"CLAIM","eval":"N/A","dtg":"2015/03/03 18:21:45","commit":"N/A","text":"Given that the group was asked what is the temperature of your cold water?","source":"","type":"I","annot":{"conc":{},"prem_assump":{"02f7261d-7fd1-437b-947e-a39c4d076866":{"cqs":[],"assump":[],"id":"LCS","prem":["PCS1"]}}},"nodeID":"ed699595-b56a-4596-957f-35f0f948a3bf","uncert":"Confirmed"},{"input":"CLAIM","eval":"N/A","dtg":"2015/03/03 18:21:45","commit":"N/A","text":"Answer 21.1 is generally accepted as true","source":"","type":"I","annot":{"conc":{},"prem_assump":{"02f7261d-7fd1-437b-947e-a39c4d076866":{"cqs":[],"assump":[],"id":"LCS","prem":["PCS2"]}}},"nodeID":"a3699fcb-e1d2-407e-80bb-aa290482dcbb","uncert":"Confirmed"},{"input":"CLAIM","eval":"N/A","dtg":"2015/03/03 18:21:45","commit":"N/A","text":"Answer 21.1 may be plausibly be true","source":"","type":"I","annot":{"prem_assump":{},"conc":{"02f7261d-7fd1-437b-947e-a39c4d076866":{"cqs":[],"id":"LCS","conc":["CCS1"]}}},"nodeID":"0c3a1fcf-e4fe-4443-b137-49f9d35ed3a0","uncert":"Confirmed"},{"dtg":"2015/03/03 18:21:45","text":"LCS","source":"","type":"RA","annot":{"id":"LCS"},"nodeID":"02f7261d-7fd1-437b-947e-a39c4d076866"},{"input":"CLAIM","eval":"N/A","dtg":"2015/03/03 18:21:45","commit":"N/A","text":"Given that the group was asked What is the color of your tap water?","source":"","type":"I","annot":{"conc":{},"prem_assump":{"81d53536-ef40-42d1-b367-5b13e9210c25":{"cqs":[],"assump":[],"id":"LCS","prem":["PCS1"]}}},"nodeID":"eb5bc293-4221-4176-b834-9d150ee80c1f","uncert":"Confirmed"},{"input":"CLAIM","eval":"N/A","dtg":"2015/03/03 18:21:45","commit":"N/A","text":"Answer Clear  is generally accepted as true","source":"","type":"I","annot":{"conc":{},"prem_assump":{"81d53536-ef40-42d1-b367-5b13e9210c25":{"cqs":[],"assump":[],"id":"LCS","prem":["PCS2"]}}},"nodeID":"addb4f68-5086-4e64-a801-ffeaab7fe3ae","uncert":"Confirmed"},{"input":"CLAIM","eval":"N/A","dtg":"2015/03/03 18:21:45","commit":"N/A","text":"Answer Clear  may plausibly be true","source":"","type":"I","annot":{"prem_assump":{},"conc":{"81d53536-ef40-42d1-b367-5b13e9210c25":{"cqs":[],"id":"LCS","conc":["CCS1"]}}},"nodeID":"47990df4-6134-42ba-9440-51da8f3bed53","uncert":"Confirmed"},{"dtg":"2015/03/03 18:21:45","text":"LCS","source":"","type":"RA","annot":{"id":"LCS"},"nodeID":"81d53536-ef40-42d1-b367-5b13e9210c25"},{"dtg":"2015/03/03 18:21:45","text":"Pro","source":"","type":"RA","annot":{"id":"N/A"},"nodeID":"bc759366-ba1f-401e-bc9b-58166523108a"},{"dtg":"2015/03/03 18:21:45","text":"Con","source":"","type":"CA","annot":{"cq_id":null,"cq_ans_node_id":null},"nodeID":"64d23575-bda6-449e-947d-a6cc15539e56"}],"edges":[{"toID":"02f7261d-7fd1-437b-947e-a39c4d076866","formEdgeID":null,"edgeID":"45f89b99-fc82-4370-afe1-bd68581e4222","fromID":"ed699595-b56a-4596-957f-35f0f948a3bf"},{"toID":"02f7261d-7fd1-437b-947e-a39c4d076866","formEdgeID":null,"edgeID":"126418af-39d1-4cf2-b607-7c8a1004a663","fromID":"a3699fcb-e1d2-407e-80bb-aa290482dcbb"},{"toID":"0c3a1fcf-e4fe-4443-b137-49f9d35ed3a0","formEdgeID":null,"edgeID":"150935be-dd44-487a-a04f-8e5cdaefea56","fromID":"02f7261d-7fd1-437b-947e-a39c4d076866"},{"toID":"81d53536-ef40-42d1-b367-5b13e9210c25","formEdgeID":null,"edgeID":"c6923c0d-d558-41dc-bcdf-8c6103de538a","fromID":"eb5bc293-4221-4176-b834-9d150ee80c1f"},{"toID":"81d53536-ef40-42d1-b367-5b13e9210c25","formEdgeID":null,"edgeID":"e9438095-e81d-48ca-bfcc-ff60794c63be","fromID":"addb4f68-5086-4e64-a801-ffeaab7fe3ae"},{"toID":"47990df4-6134-42ba-9440-51da8f3bed53","formEdgeID":null,"edgeID":"3040834c-5544-4f6d-9025-143be94544f1","fromID":"81d53536-ef40-42d1-b367-5b13e9210c25"},{"toID":"619d208d-c36e-402a-899c-8f83f0fdbef8","formEdgeID":null,"edgeID":"e24d5b49-2417-43cf-9c9e-a89f0962118f","fromID":"bc759366-ba1f-401e-bc9b-58166523108a"},{"toID":"bc759366-ba1f-401e-bc9b-58166523108a","formEdgeID":null,"edgeID":"170a2618-b14d-4b88-8d0b-3ef3f3e5709c","fromID":"0c3a1fcf-e4fe-4443-b137-49f9d35ed3a0"},{"toID":"619d208d-c36e-402a-899c-8f83f0fdbef8","formEdgeID":null,"edgeID":"232ec1a2-994a-4820-9581-ac80b0dfef69","fromID":"64d23575-bda6-449e-947d-a6cc15539e56"},{"toID":"64d23575-bda6-449e-947d-a6cc15539e56","formEdgeID":null,"edgeID":"704c0093-fd65-407d-a582-0bbb2512c665","fromID":"47990df4-6134-42ba-9440-51da8f3bed53"}],"prov":{"http://www.itacispaces.org/Process_CS_Res_8e526a1d-be7c-4d51-97b9-e6df16bdde54":{"http://www.w3.org/1999/02/22-rdf-syntax-ns#type":[{"type":"uri","value":"http://www.w3.org/ns/prov#Activity"}],"http://www.w3.org/ns/prov#wasInformedBy":[{"type":"uri","value":"http://www.itacispaces.org/Collect_CS_Res_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}],"http://www.w3.org/ns/prov#used":[{"type":"uri","value":"http://www.itacispaces.org/TASK_8e526a1d-be7c-4d51-97b9-e6df16bdde54"},{"type":"uri","value":"http://www.itacispaces.org/Data_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}],"http://www.w3.org/ns/prov#wasAssociatedWith":[{"type":"uri","value":"http://www.itacispaces.org/CISpaces_HDC_Service"}]},"_:32c1f926:14be0d09ec0:-7fab":{"http://www.w3.org/ns/prov#atTime":[{"type":"literal","value":"2015-03-03T06:21:45Z","datatype":"http://www.w3.org/2001/XMLSchema#dateTime"}],"http://www.w3.org/ns/prov#Activity":[{"type":"uri","value":"http://www.itacispaces.org/Analyse_CS_Res_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}]},"http://www.itacispaces.org/Data_8e526a1d-be7c-4d51-97b9-e6df16bdde54":{"http://www.w3.org/ns/prov#qualifiedGeneration":[{"type":"bnode","value":"_:32c1f926:14be0d09ec0:-7faf"}],"http://www.w3.org/1999/02/22-rdf-syntax-ns#type":[{"type":"uri","value":"http://www.w3.org/ns/prov#Entity"}],"http://www.w3.org/ns/prov#wasGeneratedBy":[{"type":"uri","value":"http://www.itacispaces.org/Collect_CS_Res_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}]},"_:32c1f926:14be0d09ec0:-7faa":{"http://www.w3.org/ns/prov#atTime":[{"type":"literal","value":"2015-03-03T06:21:45Z","datatype":"http://www.w3.org/2001/XMLSchema#dateTime"}],"http://www.w3.org/ns/prov#Activity":[{"type":"uri","value":"http://www.itacispaces.org/Analyse_CS_Res_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}]},
 "http://www.itacispaces.org/CSAnalysis5490cf28-625e-4264-b312-f21ee744c386":{"http://www.itacispaces.org/ns#marker":[{"type":"uri","value":"http://www.itacispaces.org/ns#Last"}]},
 "http://www.itacispaces.org/RI_8e526a1d-be7c-4d51-97b9-e6df16bdde54":{"http://www.w3.org/1999/02/22-rdf-syntax-ns#type":[{"type":"uri","value":"http://www.w3.org/ns/prov#Entity"}],"http://www.w3.org/ns/prov#type":[{"type":"uri","value":"http://www.itacispaces.org/ns#Goal"}]},"http://www.itacispaces.org/Analyse_CS_Res_8e526a1d-be7c-4d51-97b9-e6df16bdde54":{"http://www.w3.org/1999/02/22-rdf-syntax-ns#type":[{"type":"uri","value":"http://www.w3.org/ns/prov#Activity"}],"http://www.w3.org/ns/prov#used":[{"type":"uri","value":"http://www.itacispaces.org/Results_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}],"http://www.w3.org/ns/prov#wasAssociatedWith":[{"type":"uri","value":"http://www.itacispaces.org/Joe"}]},"http://www.itacispaces.org/CISpaces_of_Joe":{"http://www.w3.org/1999/02/22-rdf-syntax-ns#type":[{"type":"uri","value":"http://www.w3.org/ns/prov#Agent"}]},"http://www.itacispaces.org/Results_8e526a1d-be7c-4d51-97b9-e6df16bdde54":{"http://www.w3.org/ns/prov#qualifiedGeneration":[{"type":"bnode","value":"_:32c1f926:14be0d09ec0:-7fb0"}],"http://www.w3.org/1999/02/22-rdf-syntax-ns#type":[{"type":"uri","value":"http://www.w3.org/ns/prov#Entity"}],"http://www.w3.org/ns/prov#wasGeneratedBy":[{"type":"uri","value":"http://www.itacispaces.org/Process_CS_Res_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}],"http://www.w3.org/ns/prov#wasDerivedFrom":[{"type":"uri","value":"http://www.itacispaces.org/Data_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}]},"http://www.itacispaces.org/Node47990df4-6134-42ba-9440-51da8f3bed53":{"http://www.w3.org/ns/prov#qualifiedGeneration":[{"type":"bnode","value":"_:32c1f926:14be0d09ec0:-7fa8"}],"http://www.w3.org/1999/02/22-rdf-syntax-ns#type":[{"type":"uri","value":"http://www.w3.org/ns/prov#Entity"}],"http://www.itacispaces.org/ns#nodeID":[{"type":"literal","value":"47990df4-6134-42ba-9440-51da8f3bed53","datatype":"http://www.w3.org/2001/XMLSchema#string"}],"http://www.w3.org/ns/prov#wasGeneratedBy":[{"type":"uri","value":"http://www.itacispaces.org/Analyse_CS_Res_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}],"http://www.itacispaces.org/ns#infText":[{"type":"literal","value":"Answer Clear  may plausibly be true","datatype":"http://www.w3.org/2001/XMLSchema#string"}],"http://www.w3.org/ns/prov#type":[{"type":"uri","value":"http://www.itacispaces.org/ns#Node"}],"http://www.w3.org/ns/prov#wasDerivedFrom":[{"type":"uri","value":"http://www.itacispaces.org/Results_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}]},"_:32c1f926:14be0d09ec0:-7fad":{"http://www.w3.org/ns/prov#atTime":[{"type":"literal","value":"2015-03-03T06:21:45Z","datatype":"http://www.w3.org/2001/XMLSchema#dateTime"}],"http://www.w3.org/ns/prov#Activity":[{"type":"uri","value":"http://www.itacispaces.org/Analyse_CS_Res_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}]},"_:32c1f926:14be0d09ec0:-7fac":{"http://www.w3.org/ns/prov#atTime":[{"type":"literal","value":"2015-03-03T06:21:45Z","datatype":"http://www.w3.org/2001/XMLSchema#dateTime"}],"http://www.w3.org/ns/prov#Activity":[{"type":"uri","value":"http://www.itacispaces.org/Analyse_CS_Res_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}]},"_:32c1f926:14be0d09ec0:-7faf":{"http://www.w3.org/ns/prov#atTime":[{"type":"literal","value":"2015-03-01T06:07:03Z","datatype":"http://www.w3.org/2001/XMLSchema#dateTime"}],"http://www.w3.org/ns/prov#Activity":[{"type":"uri","value":"http://www.itacispaces.org/Collect_CS_Res_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}]},"_:32c1f926:14be0d09ec0:-7fae":{"http://www.w3.org/ns/prov#atTime":[{"type":"literal","value":"2015-03-03T06:21:45Z","datatype":"http://www.w3.org/2001/XMLSchema#dateTime"}],"http://www.w3.org/ns/prov#Activity":[{"type":"uri","value":"http://www.itacispaces.org/Analyse_CS_Res_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}]},"_:32c1f926:14be0d09ec0:-7fb1":{"http://www.w3.org/ns/prov#atTime":[{"type":"literal","value":"2015-03-01T06:26:00Z","datatype":"http://www.w3.org/2001/XMLSchema#dateTime"}],"http://www.w3.org/ns/prov#Activity":[{"type":"uri","value":"http://www.itacispaces.org/Prepare_CS_Task_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}]},"_:32c1f926:14be0d09ec0:-7fb0":{"http://www.w3.org/ns/prov#atTime":[{"type":"literal","value":"2015-03-03T00:45:01Z","datatype":"http://www.w3.org/2001/XMLSchema#dateTime"}],"http://www.w3.org/ns/prov#Activity":[{"type":"uri","value":"http://www.itacispaces.org/Process_CS_Res_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}]},"http://www.itacispaces.org/Joe":{"http://www.w3.org/1999/02/22-rdf-syntax-ns#type":[{"type":"uri","value":"http://www.w3.org/ns/prov#Agent"}]},"_:32c1f926:14be0d09ec0:-7fb2":{"http://www.w3.org/ns/prov#atTime":[{"type":"literal","value":"2015-03-03T06:21:46Z","datatype":"http://www.w3.org/2001/XMLSchema#dateTime"}],"http://www.w3.org/ns/prov#Activity":[{"type":"uri","value":"http://www.itacispaces.org/MakeAnalysis5490cf28-625e-4264-b312-f21ee744c386"}]},"http://www.itacispaces.org/Node0c3a1fcf-e4fe-4443-b137-49f9d35ed3a0":{"http://www.w3.org/ns/prov#qualifiedGeneration":[{"type":"bnode","value":"_:32c1f926:14be0d09ec0:-7fab"}],"http://www.w3.org/1999/02/22-rdf-syntax-ns#type":[{"type":"uri","value":"http://www.w3.org/ns/prov#Entity"}],"http://www.itacispaces.org/ns#nodeID":[{"type":"literal","value":"0c3a1fcf-e4fe-4443-b137-49f9d35ed3a0","datatype":"http://www.w3.org/2001/XMLSchema#string"}],"http://www.w3.org/ns/prov#wasGeneratedBy":[{"type":"uri","value":"http://www.itacispaces.org/Analyse_CS_Res_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}],"http://www.itacispaces.org/ns#infText":[{"type":"literal","value":"Answer 21.1 may be plausibly be true","datatype":"http://www.w3.org/2001/XMLSchema#string"}],"http://www.w3.org/ns/prov#type":[{"type":"uri","value":"http://www.itacispaces.org/ns#Node"}],"http://www.w3.org/ns/prov#wasDerivedFrom":[{"type":"uri","value":"http://www.itacispaces.org/Results_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}]},"http://www.itacispaces.org/Nodeed699595-b56a-4596-957f-35f0f948a3bf":{"http://www.w3.org/ns/prov#qualifiedGeneration":[{"type":"bnode","value":"_:32c1f926:14be0d09ec0:-7fad"}],"http://www.w3.org/1999/02/22-rdf-syntax-ns#type":[{"type":"uri","value":"http://www.w3.org/ns/prov#Entity"}],"http://www.itacispaces.org/ns#nodeID":[{"type":"literal","value":"ed699595-b56a-4596-957f-35f0f948a3bf","datatype":"http://www.w3.org/2001/XMLSchema#string"}],"http://www.w3.org/ns/prov#wasGeneratedBy":[{"type":"uri","value":"http://www.itacispaces.org/Analyse_CS_Res_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}],"http://www.itacispaces.org/ns#infText":[{"type":"literal","value":"Given that the group was asked what is the temperature of your cold water?","datatype":"http://www.w3.org/2001/XMLSchema#string"}],"http://www.w3.org/ns/prov#type":[{"type":"uri","value":"http://www.itacispaces.org/ns#Node"}],"http://www.w3.org/ns/prov#wasDerivedFrom":[{"type":"uri","value":"http://www.itacispaces.org/Results_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}]},"http://www.itacispaces.org/CISpaces_HDC_Service":{"http://www.w3.org/1999/02/22-rdf-syntax-ns#type":[{"type":"uri","value":"http://www.w3.org/ns/prov#Agent"}]},"http://www.itacispaces.org/Nodea3699fcb-e1d2-407e-80bb-aa290482dcbb":{"http://www.w3.org/ns/prov#qualifiedGeneration":[{"type":"bnode","value":"_:32c1f926:14be0d09ec0:-7fac"}],"http://www.w3.org/1999/02/22-rdf-syntax-ns#type":[{"type":"uri","value":"http://www.w3.org/ns/prov#Entity"}],"http://www.itacispaces.org/ns#nodeID":[{"type":"literal","value":"a3699fcb-e1d2-407e-80bb-aa290482dcbb","datatype":"http://www.w3.org/2001/XMLSchema#string"}],"http://www.w3.org/ns/prov#wasGeneratedBy":[{"type":"uri","value":"http://www.itacispaces.org/Analyse_CS_Res_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}],"http://www.itacispaces.org/ns#infText":[{"type":"literal","value":"Answer 21.1 is generally accepted as true","datatype":"http://www.w3.org/2001/XMLSchema#string"}],"http://www.w3.org/ns/prov#type":[{"type":"uri","value":"http://www.itacispaces.org/ns#Node"}],"http://www.w3.org/ns/prov#wasDerivedFrom":[{"type":"uri","value":"http://www.itacispaces.org/Results_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}]},"http://www.itacispaces.org/Nodeaddb4f68-5086-4e64-a801-ffeaab7fe3ae":{"http://www.w3.org/ns/prov#qualifiedGeneration":[{"type":"bnode","value":"_:32c1f926:14be0d09ec0:-7fa9"}],"http://www.w3.org/1999/02/22-rdf-syntax-ns#type":[{"type":"uri","value":"http://www.w3.org/ns/prov#Entity"}],"http://www.itacispaces.org/ns#nodeID":[{"type":"literal","value":"addb4f68-5086-4e64-a801-ffeaab7fe3ae","datatype":"http://www.w3.org/2001/XMLSchema#string"}],"http://www.w3.org/ns/prov#wasGeneratedBy":[{"type":"uri","value":"http://www.itacispaces.org/Analyse_CS_Res_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}],"http://www.itacispaces.org/ns#infText":[{"type":"literal","value":"Answer Clear  is generally accepted as true","datatype":"http://www.w3.org/2001/XMLSchema#string"}],"http://www.w3.org/ns/prov#type":[{"type":"uri","value":"http://www.itacispaces.org/ns#Node"}],"http://www.w3.org/ns/prov#wasDerivedFrom":[{"type":"uri","value":"http://www.itacispaces.org/Results_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}]},"http://www.itacispaces.org/Collect_CS_Res_8e526a1d-be7c-4d51-97b9-e6df16bdde54":{"http://www.w3.org/1999/02/22-rdf-syntax-ns#type":[{"type":"uri","value":"http://www.w3.org/ns/prov#Activity"}],"http://www.w3.org/ns/prov#used":[{"type":"uri","value":"http://www.itacispaces.org/TASK_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}],"http://www.w3.org/ns/prov#wasAssociatedWith":[{"type":"uri","value":"http://www.itacispaces.org/CISpaces_of_Joe"}]},"http://www.itacispaces.org/Node619d208d-c36e-402a-899c-8f83f0fdbef8":{"http://www.w3.org/ns/prov#qualifiedGeneration":[{"type":"bnode","value":"_:32c1f926:14be0d09ec0:-7fae"}],"http://www.w3.org/1999/02/22-rdf-syntax-ns#type":[{"type":"uri","value":"http://www.w3.org/ns/prov#Entity"}],"http://www.itacispaces.org/ns#nodeID":[{"type":"literal","value":"619d208d-c36e-402a-899c-8f83f0fdbef8","datatype":"http://www.w3.org/2001/XMLSchema#string"}],"http://www.w3.org/ns/prov#wasGeneratedBy":[{"type":"uri","value":"http://www.itacispaces.org/Analyse_CS_Res_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}],"http://www.itacispaces.org/ns#infText":[{"type":"literal","value":"It the case that Form 2 support","datatype":"http://www.w3.org/2001/XMLSchema#string"}],"http://www.w3.org/ns/prov#type":[{"type":"uri","value":"http://www.itacispaces.org/ns#Node"}],"http://www.w3.org/ns/prov#wasDerivedFrom":[{"type":"uri","value":"http://www.itacispaces.org/Results_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}]},"http://www.itacispaces.org/CISpaces_ERS_Service":{"http://www.w3.org/1999/02/22-rdf-syntax-ns#type":[{"type":"uri","value":"http://www.w3.org/ns/prov#Agent"}]},"http://www.itacispaces.org/Nodeeb5bc293-4221-4176-b834-9d150ee80c1f":{"http://www.w3.org/ns/prov#qualifiedGeneration":[{"type":"bnode","value":"_:32c1f926:14be0d09ec0:-7faa"}],"http://www.w3.org/1999/02/22-rdf-syntax-ns#type":[{"type":"uri","value":"http://www.w3.org/ns/prov#Entity"}],"http://www.itacispaces.org/ns#nodeID":[{"type":"literal","value":"eb5bc293-4221-4176-b834-9d150ee80c1f","datatype":"http://www.w3.org/2001/XMLSchema#string"}],"http://www.w3.org/ns/prov#wasGeneratedBy":[{"type":"uri","value":"http://www.itacispaces.org/Analyse_CS_Res_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}],"http://www.itacispaces.org/ns#infText":[{"type":"literal","value":"Given that the group was asked What is the color of your tap water?","datatype":"http://www.w3.org/2001/XMLSchema#string"}],"http://www.w3.org/ns/prov#type":[{"type":"uri","value":"http://www.itacispaces.org/ns#Node"}],"http://www.w3.org/ns/prov#wasDerivedFrom":[{"type":"uri","value":"http://www.itacispaces.org/Results_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}]},"http://www.itacispaces.org/MakeAnalysis5490cf28-625e-4264-b312-f21ee744c386":{"http://www.w3.org/1999/02/22-rdf-syntax-ns#type":[{"type":"uri","value":"http://www.w3.org/ns/prov#Activity"}],"http://www.w3.org/ns/prov#wasAssociatedWith":[{"type":"uri","value":"http://www.itacispaces.org/CISpaces_ERS_Service"}]},"_:32c1f926:14be0d09ec0:-7fa8":{"http://www.w3.org/ns/prov#atTime":[{"type":"literal","value":"2015-03-03T06:21:45Z","datatype":"http://www.w3.org/2001/XMLSchema#dateTime"}],"http://www.w3.org/ns/prov#Activity":[{"type":"uri","value":"http://www.itacispaces.org/Analyse_CS_Res_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}]},
 "http://www.itacispaces.org/CSAnalysis5490cf28-625e-4264-b312-f21ee744c386":{"http://www.itacispaces.org/ns#analysisID":[{"type":"literal","value":"5490cf28-625e-4264-b312-f21ee744c386","datatype":"http://www.w3.org/2001/XMLSchema#string"}],"http://www.w3.org/ns/prov#qualifiedGeneration":[{"type":"bnode","value":"_:32c1f926:14be0d09ec0:-7fb2"}],"http://www.w3.org/1999/02/22-rdf-syntax-ns#type":[{"type":"uri","value":"http://www.w3.org/ns/prov#Entity"}],"http://www.w3.org/ns/prov#wasGeneratedBy":[{"type":"uri","value":"http://www.itacispaces.org/MakeAnalysis5490cf28-625e-4264-b312-f21ee744c386"}],"http://www.itacispaces.org/ns#location":[{"type":"literal","value":"ERS Crowdsourced analysis","datatype":"http://www.w3.org/2001/XMLSchema#string"}],"http://www.w3.org/ns/prov#type":[{"type":"uri","value":"http://www.itacispaces.org/ns#Analysis"}]}
 ,"_:32c1f926:14be0d09ec0:-7fa9":{"http://www.w3.org/ns/prov#atTime":[{"type":"literal","value":"2015-03-03T06:21:45Z","datatype":"http://www.w3.org/2001/XMLSchema#dateTime"}],"http://www.w3.org/ns/prov#Activity":[{"type":"uri","value":"http://www.itacispaces.org/Analyse_CS_Res_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}]},"http://www.itacispaces.org/TASK_8e526a1d-be7c-4d51-97b9-e6df16bdde54":{"http://www.w3.org/ns/prov#qualifiedGeneration":[{"type":"bnode","value":"_:32c1f926:14be0d09ec0:-7fb1"}],"http://www.w3.org/1999/02/22-rdf-syntax-ns#type":[{"type":"uri","value":"http://www.w3.org/ns/prov#Entity"}],"http://www.w3.org/ns/prov#wasGeneratedBy":[{"type":"uri","value":"http://www.itacispaces.org/Prepare_CS_Task_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}],"http://www.w3.org/ns/prov#wasDerivedFrom":[{"type":"uri","value":"http://www.itacispaces.org/RI_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}]},"http://www.itacispaces.org/Prepare_CS_Task_8e526a1d-be7c-4d51-97b9-e6df16bdde54":{"http://www.w3.org/1999/02/22-rdf-syntax-ns#type":[{"type":"uri","value":"http://www.w3.org/ns/prov#Activity"}],"http://www.w3.org/ns/prov#used":[{"type":"uri","value":"http://www.itacispaces.org/RI_8e526a1d-be7c-4d51-97b9-e6df16bdde54"}],"http://www.w3.org/ns/prov#wasAssociatedWith":[{"type":"uri","value":"http://www.itacispaces.org/CISpaces_of_Joe"}]}},"node_pos":{"a3699fcb-e1d2-407e-80bb-aa290482dcbb":[289.2457434853171,422.88618171632476],"addb4f68-5086-4e64-a801-ffeaab7fe3ae":[594.8613236949428,419.8984103124828],"0c3a1fcf-e4fe-4443-b137-49f9d35ed3a0":[233.88301134094556,300.73666158826705],"02f7261d-7fd1-437b-947e-a39c4d076866":[227.66312811884066,363.50143841045934],"619d208d-c36e-402a-899c-8f83f0fdbef8":[366.56977107492924,110.83504475757402],"81d53536-ef40-42d1-b367-5b13e9210c25":[529.4258352303436,353.92166993072914],"scale":0.878773526101142,"eb5bc293-4221-4176-b834-9d150ee80c1f":[471.773273441009,420.97813005155234],"bc759366-ba1f-401e-bc9b-58166523108a":[271.9695573375931,211.42261379474155],"47990df4-6134-42ba-9440-51da8f3bed53":[524.6795406554193,291.41176136320104],"64d23575-bda6-449e-947d-a6cc15539e56":[446.80033209267026,203.04031637497764],"ed699595-b56a-4596-957f-35f0f948a3bf":[165.33867630505705,423.76495524242597]}}
  *
  *
  *
  *
  *
  *
  */



	


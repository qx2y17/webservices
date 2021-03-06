# CISpaces Web Service Calls

## VC web service

Handles the version control of the graph analysis.

There is a  ``` HTTP_requests_inputs_outputs.pdf ``` file in docs folder that gives information about different HTTP request made by different web services.

Source file: ``` VC/src/main/java/vcservlet/VCServlet.java```


- VC/rest/getInfo

 Shows the message ``` Hello Jersey``` the web service is running successfully.


- VC/rest/user/add

Add a user with the supplied properties

@param userData JSON String of form ```{"username":"Test user","password":"password","affiliation":"None"}```


- VC/rest/login

 Handles the login request. Checks username and password

@param userData JSON String of form {"username":"Test user","password":"password"}

@return a response indicating whether the user has been validated


- VC/rest/browseAnalysis (to do)
 
 This shows all the analysis for users to browse in the browse box.

 @param userId- userid is needed to find all the analysis that user created and shared with him/her.

 @return json response of graphId for all the analysis


- VC/rest/getAnalysis

 Gets the last analysis user worked on and loads the analysis to workbox. This will be modified to load the analysis user chooses from the browse box.

 @param userID a user id coming from the client

 To do- @param graphID a graph id is needed to load the analysis user has chosen

 @return either a json representing the latest analysis this user has worked on, or a new graph id for starting a new analysis.

 To do- Return the json of the analysis used has chosen to view.

 ``` JSON response:
 {"nodes":[{"input":"PREF","eval":"N\/A","dtg":"2017-09-26 13:49:28.0","islocked":"false","text":"PREF","source":"user","cmt":"N\/A","type":"P","annot":"N\/A","graphID":"de9f57b3-3183-43ec-a34c-10eb33158081","nodeID":"db66b909-b866-4479-ca8c-db575c043405","uncert":"Confirmed"},{"input":"INFO","eval":"N\/A","dtg":"2017-09-26 13:49:11.0","islocked":"false","text":"INFO","source":"user","cmt":"N\/A","type":"I","annot":"N\/A","graphID":"de9f57b3-3183-43ec-a34c-10eb33158081","nodeID":"f4f7285b-fabe-42a2-9238-4703e1072d27","uncert":"Confirmed"},{"input":"PRO","eval":"N\/A","dtg":"2017-09-26 13:49:12.0","islocked":"false","text":"PRO","source":"user","cmt":"N\/A","type":"RA","annot":"N\/A","graphID":"de9f57b3-3183-43ec-a34c-10eb33158081","nodeID":"39a08ad2-8dd7-4940-c067-45a4dd8f7efe","uncert":"Confirmed"}],"edges":[{"edgeID":"3af37733-4034-401f-930d-0544e93e4956","islocked":"false","source":"39a08ad2-8dd7-4940-c067-45a4dd8f7efe","formedgeid":"null","graphID":"de9f57b3-3183-43ec-a34c-10eb33158081","target":"db66b909-b866-4479-ca8c-db575c043405"},{"edgeID":"7f8c8fac-c9e4-4bb7-935b-b31233f10a3c","islocked":"false","source":"f4f7285b-fabe-42a2-9238-4703e1072d27","formedgeid":"null","graphID":"de9f57b3-3183-43ec-a34c-10eb33158081","target":"39a08ad2-8dd7-4940-c067-45a4dd8f7efe"}]}
 
 ```


- VC/rest/new

  Makes a new graph request

  @param graph a json containing the basic information for a new graph
  
  @return a response indicating whether the graph has been inserted into the database


- VC/rest/edge/{edgeid}:

 ``` POST ``` request adds edge.

 ``` DELETE ``` request deletes edge.

 @param edge the JSON for an edge coming from the front-end upon the creation of a new edge
 
 @return a response indicating whether the JSON has been processed and the relevant bits put into the database


- VC/rest/node/{nodeid}

 ``` POST``` request adds node.
 
 ``` DELETE``` request deletes node.

 @param json the JSON for a node coming from the front-end upon the creation of a new node
 
 @return a response indicating whether the JSON has been processed and the relevant bits put into the database


- VC/rest/save

 Saves the current graph on workbox

 @param graphInfo a json containing the graph id, the creator of the analysis' id and the title of the analysis
 
 @return a response indicating whether the analysis has been saved


- VC/rest/history

  Gets all the version of the graph using Graphid from history table. It's sorted from newer version to older version. 

  @param graphID a graph id

  @return all saved variations of an analysis which can be imported into the work box


- VC/rest/updateAnalysis

 Update the analysis after a history is imported and saved as the current version.


## ERS web Service

This web services uses the NLG to do the entity reasoning.

There is a ``` ersdoc.txt ``` in the ers folder that gives details of JSON request and response made.

Source file: ``` ers/src/ers/ERSServlet.java```

- ers/rest/WriteRules

 This web service performs entity reasoning to the graph and returns the claims made by the reasoner.
 
 If there is an error in the graph structure, it returns an error response.

 @param json of graph to analyse (nodes and edges)

 @param action: eval evaluates the graph; action: nlg generates the report

 @return for action eval: list of Problems of badly formed links (PROBS), list of evaluation for nodes  (COLORS), if uncertainty also a list of assignment is shown or an error message.

 @return for action nlg: NLG text for the report

## FEWS Web Service

This web service handles interaction with the Fact-Extraction service to include social media reports in an analysis.

Source file: ``` FEWS/src/main/java/fewsservlet/FEWSServlet.java ```

- fewsservlet/hello

  Say hello to the world to check the service is running correctly.

- fewsservlet/tweets

  ```POST``` Return a list of Tweets referring to a Topic.
  
  Request must contain the JSON representation of a Topic in its body.
  The 'negated' and 'genuine' properties are boolean encoded as integers where -1 represents NULL/Unknown.
 
  ```
  {
    "name": "topic name",
    "negated": -1,
    "genuine": 0
  }
  ```
 
  Returns a list of Tweets in JSON representation. e.g.:

  ```
   [
     {
       "id": 11,
       "extract": "text extract of Tweet",
       "uri": "Twitter URI"
     },
     {
       "id": 23,
       "extract": "another text extract of Tweet",
       "uri": "another Twitter URI"
     },
     ...
   ]
   ```
    
- fewsservlet/topics

  ```GET``` a list of all Topics present in the database.
  
  Returns all Topics, including combinations of 'negated' and 'genuine'.
  The 'negated' and 'genuine' properties are boolean encoded as integers where -1 represents NULL/Unknown.
  
  i.e. a Topic name may appear twice with a different value of 'negated'

  Topics are returned in JSON representation. e.g.:

  ```
  [
    {
      "name": "topic a",
      "negated": 1,
      "genuine": -1
    },
    {
      "name": "topic b",
      "negated": -1,
      "genuine": 1
    },
    ...
  ]
  ```
  
- fewsservlet/vocab

  Manage the vocabulary used by the fact-extraction engine to parse Tweets.
  Vocabulary is a list of VocabularyTopics (similar to Topics, see docstrings).
  A VocabularyTopic has: 
    - name - by convention lowercase with underscores
    - schema - the semantic type of the topic (e.g. "building"), default is "topic"
    - keywords - list of keywords, synonyms to search for in Tweets
        (e.g. topic "wall_street" may have "wall st" as a keyword)

  ```GET``` the current full vocabulary of the fact-extraction engine (state is managed by FEWS)
 
  ```POST``` add a topic for fact-extraction to index.  Accepts a single vocab topic in JSON form.
  This sends a message to fact-extraction to recompute the index and add new topics to the database.
  

## PROVSIMP Web Service

This web service keeps the provenance data for the analysis.

This web service is not called by VC web service, so it is not clear how the request and response are being made by it.

There is a ``` provsimpdoc.txt``` in the PROVSIMP folder that gives details of JSON request and response made.

Source file: ``` PROVSIMP/src/provsimp/ProvSimpServlet.java ```

- provsimp/rest/ProcProv

This web service saves the graph and all the proveneance data as RDF in the database. This web service has following actions

1. action: addnode - Adds the provenance of the node into the provenance table.

  @param nodeid, text, source, datetime group and stream

  @return success or fail message

2. action: save - To export the provenance of the node in a text file

   @param nodeid, userid to find the provenance of the node

   @return provenamnce data

3. action: load - To import provenance data from the text file

   @param node_data and provenance data in json format

   @return success or fail response

4. action: copynode - duplicated the node provenance data with a different id

   @param from(nodeid) and to(nodeid)

   @return success or fail response

5. action: getnode - gets the provenance of the node

   @param userid, nodeid

   @return provenance data of the node 



## PROVPLOT Web Service

Provplot web service generates the provenance image from the provplot web service provenance data. 

This web service is not implemented yet. To do - add the code to the current cispaces.

There is a ``` provgraphsdoc.txt ``` in the provplot folder that gives details of JSON request and response made.

- provgraphs/Prov?action=printprov&json=JSON

Source file: ``` provgraphs/src/plots/PlotProvServlet.java ```

@param json of the node provenance

@retun png of the provenance graph

## Info Web Service

Info web service used to handle the log in feature. Now, this is done by the VC web service.

There is a ``` infodoc.txt``` in the info folder that gives details of JSON request and response made.

- info/rest/PostInfo

Source file: ``` info/src/info/INFInputServlet.java```

This used to creates the graph and tracks provenance of the graph.

- info/rest/GetInfo

Source file: ``` info/src/info/INFOServlet.java```

A request is made with username, password and affiliation. True or false response is made if it is successful or not.
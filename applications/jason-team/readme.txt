/* 
 * Jason Team for the
 * Multi-Agent Programming Contest 2008
 * (http://cig.in.tu-clausthal.de/agentcontest2008)
 * 
 * By 
 *   Jomi F. Hubner     (EMSE, France) 
 *   Rafael  H. Bordini (Durham, UK)
 *   Gauthier Picard    (EMSE, France)
 */


To run this team:

1. update sources of Jason and JMoise+
     cd Jason-svn
     svn update
     ant plugin
   
2. run massim-server
     cd applications/jason-team/massim-server
     ./startServer.sh

   or some configuration for test
     ./startServerTest.sh conf/<some conf file>.xml

3. run dummies (written in Jason)
     ant -f dummies.xml
 
   or those written by the Contest developers  
     cd ../massim-agents
     ./startAgents.sh

4. run Jason team 

     ant -f gauchos.xml

5. start the simulation
   go to shell running startServer.sh and press ENTER

6. you can get the agents location with the command
   
         tail -f world-status.txt 

   you can see the agent mind state with

         ant -f gauchos.xml mind-view
         
   or in the files stored in directory mind-ag-gaucho1

7. to enable/disable the graphical view of some agent, add gui=yes
   or gui=no in the agent's option (.mas2j file)
   
   

---- OLD ----

Our team can run in three configurations (Jason 1.0.2 is required):

1. With a local simulator developed by us to test the team. 

   Using JasonIDE, open the project Local-JasonTeam.mas2j and
   run it.

2. With the competition simulator running at localhost.

   Using JasonIDE, open the project AC-Local-JasonTeam.mas2j 
   and run it.

3. With the competition simulator (during the tournament)

   Using JasonIDE, open the project AC-JasonTeam.mas2j 
   and run it.


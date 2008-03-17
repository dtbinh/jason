/* 
 * Jason Team for the
 * Multi-Agent Programming Contest 2008
 * (http://cig.in.tu-clausthal.de/agentcontest2008)
 * 
 * By 
 *   Jomi F. Hubner     (EMSE, France) 
 *   Rafael  H. Bordini (Durhma, UK)
 *   Gauthier Picard    (EMSE, France)
 */


To run this team:

1. update sources of Jason
   cd Jason-svn
   svn update
   ant plugin
   
2. run massim-server
   cd applications/jason-team/massim-server
   ./startServer.sh

3. run massim-agents (6 agents developed by the ContestTeam)
   cd ../massim-agents
   ./startAgents.sh

4. run Jason dummies (for now we do not have a team)
   a. by JasonIDE
      	 ../../bin/jason.sh
      open an run  AC-Local-JasonTeam.mas2j

   b. by Ant (only after run once by JasonIDE)
         ant -f bin/build.xml

5. start the simulation
   go to shell running startServer.sh and press ENTER


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


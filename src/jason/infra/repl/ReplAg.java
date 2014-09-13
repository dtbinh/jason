package jason.infra.repl;

import jason.JasonException;
import jason.asSemantics.Agent;
import jason.asSemantics.IntendedMeans;
import jason.asSemantics.Intention;
import jason.asSemantics.Option;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanBody;
import jason.asSyntax.Trigger;
import jason.util.asl2html;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import org.w3c.dom.Document;

public class ReplAg extends Agent {
    
    asl2html agTransformer = new asl2html("/xml/agInspection.xsl");
    JTextPane mindPanel = null;
    JTextField command = null;
    JTextArea output = null;
    
    String[] replCmds = { 
            clear.class.getName(),
            verbose.class.getName(),
            mi.class.getName()};
    
    int cmdCounter = 0;
    
    @Override
    public void initAg() {
        super.initAg();
        initGui();
        initLogger();
        if (mindPanel != null) {
            new Thread("update mind thread") {
                public void run() {
                    while (getTS().getUserAgArch().isRunning()) {
                        try {
                            updateMindView();                
                            sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
            }.start();
        }
    }
    
    void initLogger() {
        getTS().getLogger().addHandler( new StreamHandler() {
            @Override
            public synchronized void publish(LogRecord l) {
                output.append(l.getMessage()+"\n");
                output.setCaretPosition( output.getDocument().getLength());
            }
        });
    }

    @Override
    public void load(String asSrc) throws JasonException {
        super.load(null);
        /*try {
            getPL().add(ASSyntax.parsePlan("+!run_repl_expr(Cmd__TR) <- Cmd__TR; jason.infra.repl.print_unifier."));
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
    }
    
    void execCmd() {
        try {
            String sCmd = command.getText().trim();
            if (sCmd.endsWith(".")) 
                sCmd = sCmd.substring(0,sCmd.length()-1);
            for (String c: replCmds) {
                if (c.endsWith(sCmd) && sCmd.startsWith(".")) {
                    sCmd = c;
                    break;
                }
            }
            if (sCmd.startsWith(".verbose")) {
                sCmd = verbose.class.getPackage().getName() + sCmd;
            }
            sCmd += ";"+print_unifier.class.getName();
            PlanBody lCmd = ASSyntax.parsePlanBody(sCmd);
            Trigger  te   = ASSyntax.parseTrigger("+!run_repl_expr");
            Intention i   = new Intention();
            i.push(new IntendedMeans(
                    new Option(
                            new Plan(null,te,null,lCmd),
                            new Unifier()), 
                    te));
            //Literal g = ASSyntax.createLiteral("run_repl_expr", lCmd);
            //getTS().getLogger().info("running "+i);
            //getTS().getC().addAchvGoal(g, null);
            getTS().getC().addIntention(i);
            cmdCounter++;
            command.setText("");
            getTS().getUserAgArch().wake();
        } catch (Exception e) {
            output.setText("Error parsing "+command.getText()+"\n"+e);
        }        
    }
    
    static int lastPos = 30;
    
    void initGui() {
        command = new JTextField(40);
        command.setToolTipText("Type a Jason operation here.");
        command.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                execCmd();
            }
        });

        //mindPanel = new JTextPane();
        //mindPanel.setEditable(false);
        //mindPanel.setContentType("text/html");
        
        output = new JTextArea(5,50);
        output.setEditable(false);
        output.setText("Example of operations you can type:\n   +bel; !goal; .add_plan({+!goal <- .print(ok) }); !!goal; \n   .send(bob,tell,hello);\n");
        output.append("   ?bel(A); .findall(X,bel(X),L); \n");
        output.append("   .mi // to open mind inspector\n");
        output.append("   .verbose(2) // to show debug messages\n");
        output.append("   .clear // clean console\n");
        output.append("\nYou can add more agents using the button 'new REPL ag' in MAS Console.");
        
        output.append("\n");
        

        JFrame f = new JFrame(".::  REPL Interface for "+getTS().getUserAgArch().getAgName()+"  ::.");
        f.getContentPane().setLayout(new BorderLayout());
        f.getContentPane().add(BorderLayout.NORTH,command);    
        //f.getContentPane().add(BorderLayout.CENTER, new JScrollPane(mindPanel));
        f.getContentPane().add(BorderLayout.CENTER,new JScrollPane(output));

        f.pack();
        int h = 200;
        int w = (int)(h*2*1.618);
        f.setBounds((int)(h*0.618), 20, w, h);
        f.setLocation(lastPos, 200+lastPos);
        lastPos += 50;
        f.setVisible(true);
    }
    
    public void clear() {
        output.setText("");
    }
    
    private String lastMind = "";
    
    void updateMindView() {
        getTS().getUserAgArch().setCycleNumber(cmdCounter);
        Document agState = getAgState(); // the XML representation of the agent's mind
        try {
            String sMind = agTransformer.transform(agState); // transform to HTML
            if (!sMind.equals(lastMind))
                mindPanel.setText(sMind); // show the HTML in the screen
            lastMind = sMind;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }         
    }
}

package gui;

import jason.JasonException;
import jason.asSemantics.Intention;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.asSyntax.parser.ParseException;
import jason.mas2j.ClassParameters;
import jason.runtime.Settings;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import jmoise.OrgAgent;

public class OrgAgentGUI extends OrgAgent {

    @Override
    public void initAg(String agClass, ClassParameters bbPars, String asSrc, Settings stts) throws JasonException {
        super.initAg(agClass, bbPars, asSrc, stts);
        startGUI();
    }

    @Override
    public void stopAg() {
        super.stopAg();
        frame.setVisible(false);
    }
    
    JTextArea bels = new JTextArea(20,50);
    JTextField args = new JTextField(30);
    JFrame frame;
    
    String[][] actions = { { "create_group",   "g1,wpgroup" },
                           { "remove_group",   "g1" },
                           { "adopt_role",     "editor,g1" },
                           { "adopt_role",     "writer,g1" },
                           { "remove_role",    "role,groupId" },
                           { "create_scheme",  "p1, writePaperSch, [g1]" },
                           { "abort_scheme",   "schemeId" },
                           { "remove_scheme",   "schemeId" },
                           { "add_responsible_group", "schemeId, groupId" },
                           { "commit_mission", "mManager, p1" },
                           { "commit_mission", "mColaborator, p1" },
                           { "commit_mission", "mBib, p1" },
                           { "remove_mission", "[missionId,] SchemeId" },
                           { "set_goal_state", "p1, goalId, state" }
    };
    
    protected void startGUI() {
        
        // top
        JScrollPane pbb = new JScrollPane(bels);
        pbb.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Belief Base", TitledBorder.LEFT, TitledBorder.TOP));
        listBels();
        
        // south
        //control.add(new JLabel("Organisational action: "));
        
        final JComboBox actionsCB = new JComboBox();
        for (String[] a: actions) {
            actionsCB.addItem(a[0]);
        }
        actionsCB.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ievt) {
                int a = actionsCB.getSelectedIndex();
                args.setText(getInternalAction(a));
            }            
        });
        
        args.setText(getInternalAction(0));
        
        JButton doBT = new JButton("Do");
        doBT.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        executeAction(args.getText());
                    }
                });
            }
        });
        args.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        executeAction(args.getText());
                    }
                });
            }
        });
        

        JButton listBT = new JButton("Update Beliefs");
        listBT.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        listBels();
                    }
                });
            }
        });


        JPanel control = new JPanel(new FlowLayout());
        control.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Actions", TitledBorder.LEFT, TitledBorder.TOP));
        control.add(actionsCB);
        control.add(args);
        control.add(doBT);
        control.add(listBT);
        
        
        frame = new JFrame(":: "+getAgName()+" interface ::");
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(BorderLayout.CENTER, pbb);
        frame.getContentPane().add(BorderLayout.SOUTH, control);
        
        frame.pack();
        frame.setVisible(true);
    } 

    private String getInternalAction(int i) {
        return "jmoise."+actions[i][0] + "(" + actions[i][1] + ")";
    }
    
    protected void executeAction(String actionText) {
        bels.setText("Doing: "+ actionText);
        try {
            Term ia = ASSyntax.parsePlanBody(actionText);
            Literal action = ASSyntax.createLiteral("do", ia);
            getTS().getC().addAchvGoal(action, Intention.EmptyInt);
            getArchInfraTier().wake();
        } catch (ParseException e) {
            bels.setText("Error parsing "+actionText+" as an Jason internal action!\n"+e);
        } catch (Exception e) {
            bels.setText("Error performing action "+actionText+": "+e);
            e.printStackTrace();
        }
    }
    
    public void listBels() {
        bels.setText("");
        for (Literal b: getTS().getAg().getBB()) {
            bels.append(b+"\n");
        }
    }
    
    
    
}

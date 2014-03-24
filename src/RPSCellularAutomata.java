import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**

@author Joey Bloom
 */
public class RPSCellularAutomata
{
    private JFrame frame;
        private JPanel RPSPanel;
        private JPanel controlPanel;
            private JPanel neighborPanel;
                private JRadioButton vonNeumannButton;
                private JRadioButton radiusButton;
                private JComboBox<Integer> radiusBox;
                private ButtonGroup neighborButtonGroup;
//            private JPanel colorPanel;
//                private JComboBox<Integer> colorBox;
            private JPanel pixelSizePanel;
                private JSlider pixelSizeSlider;
            private JPanel buttonsPanel;
                private JButton playPause;
                private JButton reset;
//                private JButton applySettings;
        private JPanel speedPanel;
            private JSlider speedSlider;

    private RPS[][] grid;
    private int radius = 4; //0 means von neumann
    private int speed = 1;
//    private int colors = 3;
    private int pixelSize = 1;
    private boolean paused;

    /**
    An RPS can be in one of 4 states. It can be a Rock, Paper, Scissors, or nothing, which
    are represented as RPS.R, RPS.P, RPS.S, and RPS.X respectively.
     */
    private enum RPS
    {
        R(Color.red),
        P(Color.green),
        S(Color.blue),
        X(Color.gray);

        private RPS[] theseWillWin;
        private Color c;

        static
        {
            R.setTheseWillWin(new RPS[]{P});
            P.setTheseWillWin(new RPS[]{S});
            S.setTheseWillWin(new RPS[]{R});
            X.setTheseWillWin(new RPS[]{R,P,S,});
        }
        /**
        Constructs an RPS object associated with a Color. This constructor is private; it cannot
        be invoked outside of this enum class.
        @param c Color that this object will be displayed as
         */
        private RPS(Color c)
        {
            this.c = c;
        }

        /**
        Sets the array of RPS objects that this RPS object can be beaten by. This should only
        be called once.
        @param winners array of RPS objects that this RPS object can be beaten by.
         */
        private void setTheseWillWin(RPS[] winners)
        {
            theseWillWin = winners;
        }

        /**
        Returns the Color associated with this RPS object.
        <p/>
        The following statements always evaluate to true:<br/>
        RPS.R.getColor() == Color.red<br/>
        RPS.P.getColor() == Color.green<br/>
        RPS.S.getColor() == Color.blue<br/>
        RPS.X.getColor() == Color.gray<br/>

        @return the Color of this RPS instance
         */
        Color getColor()
        {
            return c;
        }

        /**

        @param other The RPS to check
        @return true iff other beats this
         */
        boolean isBeatenBy(RPS other)
        {
            for(RPS maybe : theseWillWin)
            {
                if(maybe == other)
                {
                    return true;
                }
            }
            return false;
        }
    }

    public RPSCellularAutomata()
    {
        setUpFrame();
        frame.setVisible(true);
        setUpGrid();
        frame.repaint();
    }

    private void setUpFrame()
    {
        frame = new JFrame();
        frame.setSize(400,450);
        frame.setTitle("Rock, Paper, Scissors Cellular Automata");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setResizable(false);

        RPSPanel = new JPanel()
        {
            @Override
            public void paintComponent(Graphics g)
            {
                if(grid == null) return;

                for(int i = 0; i < grid.length; i++)
                {
                    for(int j = 0; j < grid[0].length; j++)
                    {
                        try
                        {
                            Color c = grid[i][j].getColor();
                            g.setColor(c);
                            g.fillRect(j * pixelSize, i * pixelSize, pixelSize, pixelSize);
                        }
                        catch(NullPointerException ex)
                        {
                            System.out.println("grid[" + i + "][" + j + "] not valid");
                        }
                    }
                }
            }
        };

        frame.add(RPSPanel,BorderLayout.CENTER);

        controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(3,1));
        frame.add(controlPanel,BorderLayout.SOUTH);

            setUpNeighborPanel();

//            colorPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
//            controlPanel.add(colorPanel);
//
//                colorPanel.add(new JLabel("Colors: "));
//
//                colorBox = new JComboBox<>();
//                for(int i = 3; i <= 10; i++)
//                {
//                    colorBox.addItem(i);
//                }
//                colorBox.addActionListener(new ActionListener(){
//                    @Override
//                    public void actionPerformed(ActionEvent ev)
//                    {
//                        colors = (int)colorBox.getSelectedItem();
//                    }
//                });
//                colorPanel.add(colorBox);

            pixelSizePanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
            controlPanel.add(pixelSizePanel);

                pixelSizePanel.add(new JLabel("Pixel Size: "));

                pixelSizeSlider = new JSlider(JSlider.HORIZONTAL,1,10,1);
                pixelSizeSlider.setSnapToTicks(true);
                pixelSizeSlider.setMajorTickSpacing(1);
                pixelSizeSlider.setPaintTicks(true);
                pixelSizeSlider.setLabelTable(pixelSizeSlider.createStandardLabels(1));
                pixelSizeSlider.setPaintLabels(true);
                pixelSizeSlider.addChangeListener(new ChangeListener(){
                    @Override
                    public void stateChanged(ChangeEvent e)
                    {
                        pixelSize = pixelSizeSlider.getValue();
                        reset.doClick();
                        frame.repaint();
                    }
                });
                pixelSizePanel.add(pixelSizeSlider);

            setUpButtonsPanel();

        speedPanel = new JPanel();
        frame.add(speedPanel,BorderLayout.EAST);

            speedPanel.add(new JLabel("Speed"));
            speedSlider = new JSlider(JSlider.VERTICAL,0,3000,1000);
            speedSlider.setMajorTickSpacing(1000);
            speedSlider.setPaintTicks(true);
            Hashtable labelTable = new Hashtable();
            labelTable.put(new Integer(0), new JLabel("Fast"));
            labelTable.put(new Integer(3000), new JLabel("Slow"));
            speedSlider.setLabelTable(labelTable);
            speedSlider.setPaintLabels(true);
            speedSlider.addChangeListener(new ChangeListener(){
                @Override
                public void stateChanged(ChangeEvent e)
                {
                    speed = speedSlider.getValue();
                }
            });

            speedPanel.add(speedSlider);
    }

    private void setUpNeighborPanel()
    {
        neighborPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        controlPanel.add(neighborPanel);

            neighborPanel.add(new JLabel("Neighbors: "));
            vonNeumannButton = new JRadioButton("von Neumann");
            vonNeumannButton.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent ev)
                {
                    radiusBox.setEnabled(false);
                    radius = 0;
                }
            });
            neighborPanel.add(vonNeumannButton);

            radiusButton = new JRadioButton("radius");
            radiusButton.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent ev)
                {
                    radiusBox.setEnabled(true);
                    radius = (int)radiusBox.getSelectedItem();
                }
            });
            neighborPanel.add(radiusButton);

            radiusBox = new JComboBox<>();
            for(int i = 1; i <= 15; i++)
            {
                radiusBox.addItem(i);
            }
            radiusBox.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent ev)
                {
                    radius = (int)radiusBox.getSelectedItem();
                }
            });
            neighborPanel.add(radiusBox);

            neighborButtonGroup = new ButtonGroup();
            neighborButtonGroup.add(vonNeumannButton);
            neighborButtonGroup.add(radiusButton);

            radiusBox.setSelectedIndex(1);
            radiusButton.doClick();
    }

    private void setUpButtonsPanel()
    {
        buttonsPanel = new JPanel();
        controlPanel.add(buttonsPanel);

            playPause = new JButton("Play");
            paused = true;
            playPause.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent ev)
                {
                    if(paused)
                    {
                        paused = false;
                        playPause.setText("Pause");
                        reset.setEnabled(false);
                        pixelSizeSlider.setEnabled(false);
                        new SwingWorker<Void,Void>(){
                            @Override
                            protected Void doInBackground() throws Exception
                            {
                                while(!paused)
                                {
                                    nextItr();
                                    RPSPanel.repaint();
                                    Thread.sleep((int)Math.pow(10,speed/1000.0)-1);
                                }
                                return null;
                            }
                        }.execute();
                    }
                    else
                    {
                        paused = true;
                        playPause.setText("Play ");
                        reset.setEnabled(true);
                        pixelSizeSlider.setEnabled(true);
                    }
                }
            });
            buttonsPanel.add(playPause);

            reset = new JButton("Reset");
            reset.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    if(!paused) playPause.doClick();
                    setUpGrid();
                    RPSPanel.repaint();
                }
            });
            buttonsPanel.add(reset);

//            applySettings = new JButton("Apply Settings");
//            buttonsPanel.add(applySettings);
    }

    private void setUpGrid()
    {
        grid = new RPS[RPSPanel.getHeight()/pixelSize][];
        for(int i = 0; i < grid.length; i++)
        {
            grid[i] = new RPS[RPSPanel.getWidth()/pixelSize];
            for(int j = 0; j < grid[i].length; j++)
            {
//                 int x = ((i + j)) % 4;
// //                 int x = (int)(Math.random() * 4);
//                 switch(x)
//                 {
//                     case 0:
//                         grid[i][j] = RPS.R;
//                         break;
//                     case 1:
//                         grid[i][j] = RPS.P;
//                         break;
//                     case 2:
//                         grid[i][j] = RPS.S;
//                         break;
//                     case 3:
//                         grid[i][j] = RPS.X;
//                         break;
//                 }
                grid[i][j] = RPS.X;
            }
        }

        //setup shapes!
        for(int i = 10; i < 90; i++)
        {
            for(int j = 10; j < 90; j++)
            {
                try
                {
                    grid[i][j] = RPS.R;
                }
                catch(ArrayIndexOutOfBoundsException ex)
                {

                }
            }
        }
        for(int i = 20; i < 80; i++)
        {
            for(int j = 20; j < 80; j++)
            {
                try
                {
                    grid[i][j] = RPS.P;
                }
                catch(ArrayIndexOutOfBoundsException ex)
                {

                }
            }
        }
        for(int i = 35; i < 65; i++)
        {
            for(int j = 35; j < 65; j++)
            {
                try
                {
                    grid[i][j] = RPS.S;
                }
                catch(ArrayIndexOutOfBoundsException ex)
                {

                }
            }
        }

        for(int i = 10; i < 20; i++)
        {
            for(int j = 10; j < 90; j++)
            {
                try
                {
                    grid[i][j] = RPS.R;
                }
                catch(ArrayIndexOutOfBoundsException ex)
                {

                }
            }
        }
        for(int i = 40; i < 50; i++)
        {
            for(int j = 10; j < 90; j++)
            {
                try
                {
                    grid[i][j] = RPS.R;
                }
                catch(ArrayIndexOutOfBoundsException ex)
                {

                }
            }
        }
        for(int i = 70; i < 80; i++)
        {
            for(int j = 10; j < 90; j++)
            {
                try
                {
                    grid[i][j] = RPS.R;
                }
                catch(ArrayIndexOutOfBoundsException ex)
                {

                }
            }
        }
    }

    private static final int[] rs = {-1,0,1,0,-1,0,1,0,};
    private static final int[] cs = {0,-1,0,1,0,-1,0,1,};
    //[0][4] is up
    //[1][5] is left
    //[2][6] is down
    //[3][7] is right
    //so if we have a for loop like the following:
    //  for(int k = 1; k <= 3; k++)...
    //that means the loop goes left, down, right.

    public void nextItr()
    {
        int rowLength = grid.length;
        RPS[][] nextGrid = new RPS[rowLength][];
        int lastRow = grid.length - 1;
        int lastCol = grid[0].length - 1;

        for(int i = 0; i <= lastRow; i++)
        {
            int colLength = grid[i].length;
            nextGrid[i] = new RPS[colLength];
            loop:
            for(int j = 0; j <= lastCol; j++)
            {
                RPS thisOne = grid[i][j];

                if(radius == 0)
                {
                    //randomly check one von neumann neighbor
                    int k = (int)(Math.random()*4);
                    RPS other = grid[(rs[k]+i+rowLength)%rowLength][(cs[k]+j+colLength)%colLength];
                    if(thisOne.isBeatenBy(other))
                    {
                        nextGrid[i][j] = other;
                        continue loop;
                    }
                    else nextGrid[i][j] = thisOne;
                }
                else
                {
                    int rInc = (int)(Math.random() * (2 * radius + 1)) - radius;
                    int cInc = (int)(Math.random() * (2 * radius + 1)) - radius;
                    RPS other = grid[(i+rInc+rowLength)%rowLength][(j+cInc+colLength)%colLength];
                    if(thisOne.isBeatenBy(other))
                    {
                        nextGrid[i][j] = other;
                        continue loop;
                    }
                    else nextGrid[i][j] = thisOne;
                }
//                 //check all 4 von neumann neighbors
//                 for(int k = 0; k < 4; k++)
//                 {
//                     //this line makes pixels wrap around the edges when looking for neighbors
//                     RPS other = grid[(rs[k]+i+rowLength)%rowLength][(cs[k]+j+colLength)%colLength];
//                     if(thisOne.isBeatenBy(other))
//                     {
//                         nextGrid[i][j] = other;
//                         continue loop;
//                     }
//                 }
            }
        }
//         firstRow:
//         {
//             nextGrid[0] = new RPS[grid[0].length];
//             firstCol:
//             {
//                 RPS thisOne = grid[0][0];
//                 for(int k = 2; k <= 3; k++)
//                 {
//                     RPS other = grid[rs[k]][cs[k]];
//                     if(thisOne.isBeatenBy(other))
//                     {
//                         nextGrid[0][0] = other;
//                         break firstCol;
//                     }
//                 }
//                 nextGrid[0][0] = thisOne;
//             }
//             middleCols:
//             {
//                 loop:
//                 for(int j = 1; j < lastCol; j++)
//                 {
//                     RPS thisOne = grid[0][j];
//                     for(int k = 1; k <= 3; k++)
//                     {
//                         RPS other = grid[rs[k]][j+cs[k]];
//                         if(thisOne.isBeatenBy(other))
//                         {
//                             nextGrid[0][j] = other;
//                             continue loop;
//                         }
//                     }
//                     nextGrid[0][j] = thisOne;
//                 }
//             }
//             lastCol:
//             {
//                 RPS thisOne = grid[0][lastCol];
//                 for(int k = 1; k <= 2; k++)
//                 {
//                     RPS other = grid[rs[k]][lastCol+cs[k]];
//                     if(thisOne.isBeatenBy(other))
//                     {
//                         nextGrid[0][lastCol] = other;
//                         break lastCol;
//                     }
//                 }
//                 nextGrid[0][lastCol] = thisOne;
//             }
//         }
//         middleRows:
//         {
//             for(int i = 1; i < lastRow; i++)
//             {
//                 nextGrid[i] = new RPS[grid[i].length];
//                 firstCol:
//                 {
//                     RPS thisOne = grid[i][0];
//                     for(int k = 2; k <= 4; k++)
//                     {
//                         RPS other = grid[i+rs[k]][cs[k]];
//                         if(thisOne.isBeatenBy(other))
//                         {
//                             nextGrid[i][0] = other;
//                             break firstCol;
//                         }
//                     }
//                     nextGrid[i][0] = thisOne;
//                 }
//                 middleCol:
//                 {
//                     loop:
//                     for(int j = 1; j < lastCol; j++)
//                     {
//                         RPS thisOne = grid[i][j];
//                         //check each of the 4 neighbors (not diagonally)
//                         //to see if thisOne has been beaten and needs to be
//                         //replaced. Do not replace
//                         //neighbors, only replace thisOne.
//                         for(int k = 0; k <= 3; k++)
//                         {
// //                                 RPS replacement = grid[rs[k]][cs[k]].getReplacement();
// //                                 if(replacement == thisOne)
// //                                 {
// //                                     nextGrid[i][j] = replacement;
// //                                     continue loop;
// //                                 }
//                             RPS other = grid[i+rs[k]][j+cs[k]];
//                             if(thisOne.isBeatenBy(other))
//                             {
//                                 nextGrid[i][j] = other;
//                                 continue loop;
//                             }
//                         }
//                         //if not beaten, will stay the same for next round
//                         nextGrid[i][j] = thisOne;
//                     }
//                 }
//                 lastCol:
//                 {
//                     RPS thisOne = grid[i][lastCol];
//                     for(int k = 0; k <= 2; k++)
//                     {
//                         RPS other = grid[i+rs[k]][lastCol+cs[k]];
//                         if(thisOne == null)
//                         {
//                             System.out.println("Uh-oh");
//                         }
//                         if(thisOne.isBeatenBy(other))
//                         {
//                             nextGrid[i][lastCol] = other;
//                             break lastCol;
//                         }
//                     }
//                     nextGrid[i][lastCol] = thisOne;
//                 }
//             }
//         }
//         lastRow:
//         {
//             nextGrid[lastRow] = new RPS[grid[lastRow].length];
//             firstCol:
//             {
//                 RPS thisOne = grid[lastRow][0];
//                 for(int k = 3; k <= 4; k++)
//                 {
//                     RPS other = grid[lastRow+rs[k]][cs[k]];
//                     if(thisOne.isBeatenBy(other))
//                     {
//                         nextGrid[lastRow][0] = other;
//                         break firstCol;
//                     }
//                 }
//                 nextGrid[lastRow][0] = thisOne;
//             }
//             middleCol:
//             {
//                 loop:
//                 for(int j = 1; j < lastCol; j++)
//                 {
//                     RPS thisOne = grid[lastRow][j];
//                     for(int k = 3; k <= 5; k++)
//                     {
//                         RPS other = grid[lastRow+rs[k]][j+cs[k]];
//                         if(thisOne.isBeatenBy(other))
//                         {
//                             nextGrid[lastRow][j] = other;
//                             continue loop;
//                         }
//                     }
//                     nextGrid[lastRow][j] = thisOne;
//                 }
//             }
//             lastCol:
//             {
//                 RPS thisOne = grid[lastRow][lastCol];
//                 for(int k = 0; k <= 1; k++)
//                 {
//                     RPS other = grid[lastRow+rs[k]][lastCol+cs[k]];
//                     if(thisOne.isBeatenBy(other))
//                     {
//                         nextGrid[lastRow][lastCol] = other;
//                         break lastCol;
//                     }
//                 }
//                 nextGrid[lastRow][lastCol] = thisOne;
//             }
//         }
        grid = nextGrid;
        frame.repaint();
    }

    public static void main(String[] args) throws InterruptedException
    {
        RPSCellularAutomata inst = new RPSCellularAutomata();
//        Thread.sleep(2000);
//        while(true)
//        {
//            inst.nextItr();
//            inst.RPSPanel.repaint();
//            Thread.sleep((int)Math.pow(10,inst.speed/1000.0)-1);
//        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamechess;

import Piece.Bishop;
import Piece.Coordinate;
import Piece.EmptyCell;
import Piece.Rook;
import Piece.Knight;
import Piece.Queen;
import Piece.Piece;
import Message.Request;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import AI.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTextPane;
import java.awt.Font;
import javax.swing.JMenuBar;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.UIManager;
import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 *
 * @author nthan
 */
public class Main extends javax.swing.JFrame {

    public static final int PLAYER_PLAYER = 100;
    public static final int PLAYER_COMPUTER = 101;
    public static final int PLAYER_ONLINE = 102;
    public static final int CHALLENGE = 103;
    public static final int TO_BE_CHALLENGED = 104;
    public static int Mode;
    public int rowBefor = -1;
    public int columBefor = -1;
    private static String fileName;
    public static ArrayList<Coordinate> listCoordinate;
    public static boolean checkSelect = false;
    public static Coordinate pieceSelect = new Coordinate(-1, -1);
    public static boolean checkSaveas = false;
    public static Thread listenner;
    public static int capacity = -1;
    public static Main Mymain;
    public static boolean waitForEnemy;
    public static boolean waitAI = false;
    public List<History> history = new ArrayList<History>();  
    
    
    //--------------------------------------------------------------------------
    public static boolean checkKingWhiteMove = false;
    public static boolean checkKingBlackMove = false;
    public static boolean checkRookLeftWhiteMove = false;
    public static boolean checkRookLeftBlackMove = false;
    public static boolean checkRookRightWhiteMove = false;
    public static boolean checkRookRightBlackMove = false;
    public TheardAI aI;
    public PlayOnline playonline;
      //--------------------------------------------------------------------------
    /**
     * Creates new form Main
     */
    public Main() {

        // paint Board chess
        initComponents();
        chessBoard.removeAll();
        this.getContentPane().setBackground( Color.WHITE );
        ImageIcon icon = new ImageIcon("image/MAIN.jpg");
        JLabel label = new JLabel(); 
        label.setIcon(icon);
        chessBoard.add(label);
        chessBoard.setVisible(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try{
                    Client.requestSurrener();
                    System.out.println(InetAddress.getLocalHost().getHostAddress().toString());
                    Client.RequestDisConnect("/" + InetAddress.getLocalHost().getHostAddress().toString());
                    Client.server.close();
                }
                catch(Exception ex){}
            }
        });
        
        listenner = new Thread( new Runnable(){
            private volatile boolean running = true;

            public void terminate() {
                System.out.println("stop listener");
                running = false;
            }
            @Override
            public void run(){
                while(running){
                    Request req = Client.response();
                    switch(req.getType()){
                        case Request.ACCEPT_CHALLENGE:
                            Main.capacity = Main.CHALLENGE;
                            JOptionPane.showMessageDialog(null, "Player " +
                                   req.getIPChallenge() + " accept challenge");
                            capacity = Main.CHALLENGE;
                            waitForEnemy = true;
                            BoardChess.turn = Piece.White;
                            newGamePlayOnline();
                            break;
                        case Request.ONLINE_PLAYER:
                            PlayOnline.listPlayer = req.getPlayerList();
                            playonline.LoadListPlayerOnline();
                            break;
                        case Request.MOVE_CHESS:
                            System.err.println("CLIENT MOVE CHESS");
                            Main.Mymain.chessBoard.removeAll();
                            System.out.println(req.getStart().getRow() + "-" + req.getStart().getCol());
                            System.out.println(req.getTarget().getRow() + "-" + req.getTarget().getCol());
                            
                            
                            BoardChess.chessBoard[req.getTarget().getRow()][req.getTarget().getCol()] = 
                                    BoardChess.chessBoard[req.getStart().getRow()][req.getStart().getCol()];
                            
                            BoardChess.chessBoard[req.getTarget().getRow()]
                                    [req.getTarget().getCol()].getCoordinate().
                                    setRow(req.getTarget().getRow());
                            
                            BoardChess.chessBoard[req.getTarget().getRow()]
                                    [req.getTarget().getCol()].getCoordinate().setCol
                                    (req.getTarget().getCol());
                            
                            BoardChess.chessBoard[req.getStart().getRow()]
                                    [req.getStart().getCol()] = 
                                    new EmptyCell(-1, 
                                    new Coordinate(req.getStart().getRow(),
                                            req.getStart().getCol()));
                            
                            if(Main.Mymain.capacity == TO_BE_CHALLENGED){
                                BoardChess.ReverseChessBoard(BoardChess.chessBoard, BoardChess.chessBoardReverse);
                                BoardChess.turn = Piece.Black;
                            }
                            if(Main.Mymain.capacity == CHALLENGE){
                                BoardChess.turn = Piece.White;
                            }
                            waitForEnemy = true;
                            chessBoard.add(BoardChess.paintChessBox(new
                             EmptyCell(-1, new Coordinate(req.getStart().getRow(), -1)),null));
                            chessBoard.setVisible(true);
                            Main.Mymain.setVisible(true);
                            break;
                        case Request.REFUSE_CHALLENGE:
                            JOptionPane.showMessageDialog(null, "Player " +
                                    req.getIPChallenge() + " REFUSE");
                            break;
                        case Request.SEND_CHALLENGE:
                            int dialogButton  = 
                                    JOptionPane.showConfirmDialog
                              (null, "Would you like to play with " + req.getIPChallenge(),
                                    "Challenge", JOptionPane.YES_NO_OPTION);
                            if(dialogButton == JOptionPane.YES_OPTION) {
                                Client.requestAcceptChallenge(req.getIPChallenge());
                                Main.capacity = Main.TO_BE_CHALLENGED;
                                waitForEnemy = false;
                                newGamePlayOnline();
                            }
                            else {
                                  Client.requestRefuseChallenge(req.getIPChallenge());
                            }
                            break;
                        case Request.SURRENDER:
                            JOptionPane.showMessageDialog(null, "ENEMY SURRENDER\n" + "YOU WIN");
                            Main.Mymain.loadHome();
                            Main.Mymain.playonline.setVisible(false);
                            break;
                        case Request.PLAYING:
                            JOptionPane.showMessageDialog(null, "Player " +
                                   req.getIPChallenge() + " is playing");
                            break;
                        case Request.CROWN:
                            switch(req.getCrown()){
                                case Request.CROWNROOK:
                                    if(capacity == CHALLENGE){
                                        BoardChess.chessBoard[req.
                                           getCoordinateCrown().getRow()]
                                           [req.getCoordinateCrown().getCol()] = 
                                            new Rook(Piece.Black, req.getCoordinateCrown());
                                        
                                    }
                                    else{
                                        BoardChess.chessBoard[req.
                                           getCoordinateCrown().getRow()]
                                           [req.getCoordinateCrown().getCol()] = 
                                            new Rook(Piece.White, req.getCoordinateCrown());
                                        BoardChess.ReverseChessBoard(BoardChess.chessBoard, BoardChess.chessBoardReverse);
                                    }
                                    break;
                                case Request.CROWNQUEEN:
                                    if(capacity == CHALLENGE){
                                        BoardChess.chessBoard[req.
                                           getCoordinateCrown().getRow()]
                                           [req.getCoordinateCrown().getCol()] = 
                                            new Queen(Piece.Black, req.getCoordinateCrown());
                                    }
                                    else{
                                        BoardChess.chessBoard[req.
                                           getCoordinateCrown().getRow()]
                                           [req.getCoordinateCrown().getCol()] = 
                                            new Queen(Piece.White, req.getCoordinateCrown());
                                        BoardChess.ReverseChessBoard(BoardChess.chessBoard, BoardChess.chessBoardReverse);
                                    }
                                    break;
                                case Request.CROWNKNIGHT:
                                    if(capacity == CHALLENGE){
                                        BoardChess.chessBoard[req.
                                           getCoordinateCrown().getRow()]
                                           [req.getCoordinateCrown().getCol()] = 
                                            new Knight(Piece.Black, req.getCoordinateCrown());
                                    }
                                    else{
                                        BoardChess.chessBoard[req.
                                           getCoordinateCrown().getRow()]
                                           [req.getCoordinateCrown().getCol()] = 
                                            new Knight(Piece.White, req.getCoordinateCrown());
                                        BoardChess.ReverseChessBoard(BoardChess.chessBoard, BoardChess.chessBoardReverse);
                                    }
                                    break;
                                case Request.CROWNBISHOP:
                                    if(capacity == CHALLENGE){
                                        BoardChess.chessBoard[req.
                                           getCoordinateCrown().getRow()]
                                           [req.getCoordinateCrown().getCol()] = 
                                            new Bishop(Piece.Black, req.getCoordinateCrown());
                                    }
                                    else{
                                        BoardChess.chessBoard[req.
                                           getCoordinateCrown().getRow()]
                                           [req.getCoordinateCrown().getCol()] = 
                                            new Bishop(Piece.White, req.getCoordinateCrown());
                                        BoardChess.ReverseChessBoard(BoardChess.chessBoard, BoardChess.chessBoardReverse);
                                    }
                                    break;
                                    
                            }
                            chessBoard.removeAll();
                            chessBoard.add(BoardChess.paintChessBox(new
                             EmptyCell(-1, new Coordinate(-2, -1)),null));
                            chessBoard.setVisible(true);
                            Main.Mymain.setVisible(true);
                            break;
                               
                }
                }
            }
        });
       

        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenu1 = new javax.swing.JMenu();
        chessBoard = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        btn_surr = new javax.swing.JButton();
        lb_turn = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();

        jMenu1.setText("jMenu1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(153, 204, 0));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        chessBoard.setPreferredSize(new java.awt.Dimension(600, 600));
        chessBoard.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                chessBoardMouseClicked(evt);
            }
        });
        chessBoard.setLayout(new java.awt.GridLayout(1, 0));

        jButton4.setText("jButton4");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        btn_surr.setText("SURRENDER");
        btn_surr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_surrActionPerformed(evt);
            }
        });

        lb_turn.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lb_turn.setText("PLAY ONLINE");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btn_surr, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lb_turn, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btn_surr, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addComponent(lb_turn, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(51, Short.MAX_VALUE))
        );

        setJMenuBar(jMenuBar1);
                                
                                JPanel panel = new JPanel();
                                panel.setBackground(UIManager.getColor("Button.background"));
                                jMenuBar1.add(panel);
                                btn_home = new javax.swing.JButton();
                                btn_home.setText("HOME");
                                panel.add(btn_home);
                                
                                        btn_home.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/Home.png"))); // NOI18N
                                        btn_home.setFocusable(false);
                                        btn_home.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
                                        btn_home.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
                                        btn_home.addActionListener(new java.awt.event.ActionListener() {
                                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                                btn_homeActionPerformed(evt);
                                            }
                                        });
                                
                                JPanel panel_1 = new JPanel();
                                jMenuBar1.add(panel_1);
                                btn_withComputer = new javax.swing.JButton();
                                panel_1.add(btn_withComputer);
                                
                                		btn_withComputer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/redo.jpg"))); // NOI18N
                                		btn_withComputer.setFocusable(false);
                                		btn_withComputer.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
                                		btn_withComputer.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
                                		btn_withComputer.addActionListener(new java.awt.event.ActionListener() {
                                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                            	btn_withComputerActionPerformed(evt);
                                            }
                                        });
                                
                                JPanel panel_2 = new JPanel();
                                jMenuBar1.add(panel_2);
                                btn_withPlayer = new javax.swing.JButton();
                                panel_2.add(btn_withPlayer);
                                
		                                btn_withPlayer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/undo.png"))); // NOI18N
		                                btn_withPlayer.setFocusable(false);
		                                btn_withPlayer.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		                                btn_withPlayer.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		                                btn_withPlayer.addActionListener(new java.awt.event.ActionListener() {
                                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                            	btn_withPlayerActionPerformed(evt);
                                            }
                                        });
                                
                                JPanel panel_3 = new JPanel();
                                jMenuBar1.add(panel_3);
                                btn_training = new javax.swing.JButton();
                                panel_3.add(btn_training);
                                
		                                btn_training.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/undo.png"))); // NOI18N
		                                btn_training.setFocusable(false);
		                                btn_training.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		                                btn_training.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		                                btn_training.addActionListener(new java.awt.event.ActionListener() {
                                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                            	btn_trainingActionPerformed(evt);
                                            }
                                        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        layout.setHorizontalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        			.addGap(48)
        			.addComponent(chessBoard, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addGap(34)
        			.addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        			.addContainerGap(54, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
        	layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(layout.createSequentialGroup()
        			.addGroup(layout.createParallelGroup(Alignment.LEADING)
        				.addGroup(layout.createSequentialGroup()
        					.addGap(44)
        					.addComponent(chessBoard, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        				.addGroup(layout.createSequentialGroup()
        					.addGap(151)
        					.addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, 166, GroupLayout.PREFERRED_SIZE)))
        			.addContainerGap(46, Short.MAX_VALUE))
        );
        getContentPane().setLayout(layout);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void chessBoardMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_chessBoardMouseClicked
        
                            

        columBefor = evt.getX()/75;
        rowBefor = evt.getY()/75;
        chessBoard.removeAll();
        
        if(checkSelect == false){  
            pieceSelect.setRow(rowBefor);
            pieceSelect.setCol(columBefor);
            
            // CHECK ONLINE
            if(Mode == PLAYER_ONLINE){;
                if(capacity == TO_BE_CHALLENGED){
                    rowBefor = 7 - rowBefor;
                    columBefor = 7 - columBefor;
                }
            }
            
            // CHECK AI
            if(Mode == PLAYER_COMPUTER){
                if(!waitAI){
                    return;
                }
            }
            
            if(BoardChess.turn == BoardChess.chessBoard[rowBefor][columBefor].getColor()){
                System.out.println(BoardChess.chessBoard[rowBefor][columBefor].getName());
                listCoordinate = new ArrayList<Coordinate>(BoardChess.chessBoard[rowBefor][columBefor].getPossibleMove( BoardChess.chessBoard));
                if(waitForEnemy == false && Mode == PLAYER_ONLINE)
                    return;
                checkSelect = true;
                // CHECK RESULT: WIN OR CLOSE OR HOA
                checkResult();
        
                // PROCESS ONLINE
                if(Mode == PLAYER_ONLINE){
                    if(capacity == TO_BE_CHALLENGED){
                        for(int i = 0; i < listCoordinate.size(); i++){
                            listCoordinate.get(i).setRow(7 - listCoordinate.get(i).getRow());
                            listCoordinate.get(i).setCol(7 - listCoordinate.get(i).getCol());
                        }
                    }
                }
                
                chessBoard.add(BoardChess.paintChessBox(BoardChess.chessBoard[pieceSelect.getRow()]
                        [pieceSelect.getCol()],listCoordinate));
                chessBoard.setVisible(true);
                this.setVisible(true);
            }
            
        }
        else{
            boolean isCrown = false;  
            if(listCoordinate != null || listCoordinate.size() > 0){ 
                
                // Process Online
                if(Mode == PLAYER_ONLINE){
                    if(capacity == TO_BE_CHALLENGED){
                        pieceSelect.setRow(7 - pieceSelect.getRow());
                        pieceSelect.setCol(7 - pieceSelect.getCol());

                        for(int j = 0; j < listCoordinate.size(); j++){
                            listCoordinate.get(j).setRow(7 - listCoordinate.get(j).getRow());
                            listCoordinate.get(j).setCol(7 - listCoordinate.get(j).getCol());
                        }

                        rowBefor = 7 - rowBefor;
                        columBefor = 7 - columBefor;
                    }
                }
                

                
                for(int i = 0; i < listCoordinate.size(); i++){
                    if(rowBefor == listCoordinate.get(i).getRow() && 
                            columBefor == listCoordinate.get(i).getCol()){  


                    // SWAP AND MOVE CHESS
                    Piece temp = BoardChess.chessBoard[rowBefor][columBefor];
                    temp.getCoordinate().setRow(rowBefor);
                    temp.getCoordinate().setCol(columBefor);

                    BoardChess.chessBoard[rowBefor][columBefor] = 
                            BoardChess.chessBoard[pieceSelect.getRow()][pieceSelect.getCol()];
                    BoardChess.chessBoard[rowBefor][columBefor].getCoordinate().setRow(rowBefor);
                    BoardChess.chessBoard[rowBefor][columBefor].getCoordinate().setCol(columBefor);
                    BoardChess.chessBoard[pieceSelect.getRow()][pieceSelect.getCol()] = 
                            new EmptyCell(-1, new Coordinate(pieceSelect.getRow(), pieceSelect.getCol()));
                    // CHECK INVALID OF PIECE GO

                    // CHECK RESULT
                    checkResult();
                     
                    if(BoardChess.checkMate() == true){
                        JOptionPane.showMessageDialog(null, ""
                             + "Invalid", "Error", JOptionPane.ERROR_MESSAGE);
                        
                        // BACK PIECE GO BEFORE
                        BoardChess.chessBoard[pieceSelect.getRow()][pieceSelect.getCol()]
                                = BoardChess.chessBoard[rowBefor][columBefor];
                        BoardChess.chessBoard[pieceSelect.getRow()][pieceSelect.getCol()]
                                .getCoordinate().setRow(pieceSelect.getRow());
                        BoardChess.chessBoard[pieceSelect.getRow()][pieceSelect.getCol()]
                                .getCoordinate().setCol(pieceSelect.getCol());
                        
                         BoardChess.chessBoard[rowBefor][columBefor] = temp;
                         BoardChess.chessBoard[rowBefor][columBefor].getCoordinate().setRow(rowBefor);
                         BoardChess.chessBoard[rowBefor][columBefor].getCoordinate().setCol(columBefor);
                         return;
                    }
                    else{
                        // SEND REQUEST FOR SERVER
                        if(Mode == Main.PLAYER_ONLINE && Main.capacity == TO_BE_CHALLENGED){
                        Client.requestMoveChess(pieceSelect,    
                            new  Coordinate(rowBefor, columBefor));
                        BoardChess.ReverseChessBoard(BoardChess.chessBoard, BoardChess.chessBoardReverse);
                        waitForEnemy = false;
                        }
                        else if(Mode == Main.PLAYER_ONLINE){
                            Client.requestMoveChess(pieceSelect,
                                    new Coordinate(rowBefor, columBefor));
                            waitForEnemy = false;
                        }
                        if(Mode == PLAYER_COMPUTER){
                            this.waitAI = false;
                        }
                                             
                       // CHECK MOVE OF KING AND ROOK
                    switch (BoardChess.chessBoard[pieceSelect.getRow()][pieceSelect.getCol()].getName()) {
                        case "White_King":
                            Main.checkKingWhiteMove = true;
                            break;
                        case "Black_King":
                            Main.checkKingBlackMove = true;
                            break;
                        case "White_Rook":
                            if (pieceSelect.getCol() == 0) {
                                Main.checkRookLeftWhiteMove = true;
                            }
                            if (pieceSelect.getCol() == 7) {
                                System.out.println("checkRookRightWhiteMove");
                                Main.checkRookRightWhiteMove = true;
                            }
                            break;
                        case "Black_Rook":
                            if (pieceSelect.getCol() == 7) {
                                Main.checkRookLeftBlackMove = true;
                            }
                            if (pieceSelect.getCol() == 0) {
                                Main.checkRookRightBlackMove = true;
                            }
                            break;
                    }                   
                     // Enter into
                            
                    // ento into King Black 
                    if(BoardChess.chessBoard[rowBefor][columBefor].
                            getName().equals("Black_King")){
                        // KING ENTO INTO LEFT
                        if(rowBefor ==0 && columBefor == 2 && 
                                !Main.checkRookRightBlackMove){  
                            // PROCESS ONLINE
                            if(Mode == Main.PLAYER_ONLINE){
                                Client.requestMoveChess(new Coordinate(0, 0),
                                       new Coordinate(0, 3));
                            }
                            
                            BoardChess.chessBoard[0][3] =
                                    BoardChess.chessBoard[0][0];
                            BoardChess.chessBoard[0][3].getCoordinate().setCol(3);
                            BoardChess.chessBoard[0][0] = new EmptyCell(-1,
                            new Coordinate(0,0));



                        }
                        // KING ENTER INTO RIGHT
                        if(rowBefor == 0 && columBefor == 6 && 
                                !Main.checkRookLeftBlackMove){
                            if(Mode == Main.PLAYER_ONLINE){
                                    Client.requestMoveChess(new Coordinate(0,7),
                                            new Coordinate(0, 5));
                                }

                                BoardChess.chessBoard[0][5] =

                                        BoardChess.chessBoard[0][7];
                                BoardChess.chessBoard[0][5].getCoordinate().setCol(5);
                                BoardChess.chessBoard[0][7] = new EmptyCell(-1,
                                new Coordinate(0,7));

                            }
                        
                        // process online
                        if(capacity == TO_BE_CHALLENGED){
                            BoardChess.ReverseChessBoard(BoardChess.chessBoard,
                                    BoardChess.chessBoardReverse);
                        }
                        
                        }
                        
                        // KING WHITE ENTER INTO 
                        if(BoardChess.chessBoard[rowBefor][columBefor].
                                getName().equals("White_King")){
                            // ENTER INTO LEFT
                            if(rowBefor ==7 && columBefor == 2 && 
                                    !Main.checkRookLeftWhiteMove){
                                // PROCESS ONLINE ENTER INTO
                                if(Mode == PLAYER_ONLINE){
                                    Client.requestMoveChess(new Coordinate(7, 0),
                                            new Coordinate(7, 3));
                                }
                                BoardChess.chessBoard[7][3] =
                                        BoardChess.chessBoard[7][0];
                                BoardChess.chessBoard[7][3].getCoordinate().setCol(3);
                                BoardChess.chessBoard[7][0] = new EmptyCell(-1,
                                new Coordinate(7,0));
                                
                            }
                            if(rowBefor == 7 && columBefor == 6 && 
                                    !Main.checkRookRightWhiteMove){
                                // PEOCESS ONLINE ENTER 
                                if(Mode == PLAYER_ONLINE){
                                    Client.requestMoveChess(new Coordinate(7, 7),
                                            new Coordinate(7, 5));
                                }
                                BoardChess.chessBoard[7][5] =
                                        BoardChess.chessBoard[7][7];
                                BoardChess.chessBoard[7][5].getCoordinate().setCol(5);
                                BoardChess.chessBoard[7][7] = new EmptyCell(-1,
                                new Coordinate(7,7));
                                
                            }
                            
                            // process online
                            if(capacity == TO_BE_CHALLENGED){
                                BoardChess.ReverseChessBoard(BoardChess.chessBoard,
                                        BoardChess.chessBoardReverse);
                             }
                        }
                        
                        if(BoardChess.turn == Piece.White){
                            isCrown = Crown();
                            BoardChess.turn = Piece.Black;
                        }
                        
                        else{
                            isCrown = Crown();
                            BoardChess.turn = Piece.White;
                        }
                        
                    }  
                 }
            }
            checkSelect = false;
            if (!isCrown) {
                chessBoard.add(BoardChess.paintChessBox(new EmptyCell(-1, new Coordinate(-1, -1)),null));
                chessBoard.setVisible(true);
                this.setVisible(true);
            }
        }
        }
            
    }//GEN-LAST:event_chessBoardMouseClicked

    private void btn_trainingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_newGameActionPerformed
    	HEHE aa = new HEHE(this);
        aa.setVisible(true);
    }//GEN-LAST:event_btn_newGameActionPerformed
    
    private void btn_withComputerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_player_AIActionPerformed
    	HIHI aa = new HIHI(this);
        aa.setVisible(true);
    }//GEN-LAST:event_btn_player_AIActionPerformed
    
    private void btn_withPlayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_player_AIActionPerformed
    	HAHA aa = new HAHA(this);
        aa.setVisible(true);
    }//GEN-LAST:event_btn_player_AIActionPerformed

    private void btn_homeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_homeActionPerformed
        // paint Board chess
        checkSaveas = false;
        if(BoardChess.checkInit){
            int n = JOptionPane.showConfirmDialog(
                            null, "Do you want to exit the game?",
                            "Notification",
                            JOptionPane.YES_NO_OPTION);
            if(n == JOptionPane.YES_OPTION){
            	loadHome();
            }
            else{
            	
            }
                
        }
       

    }//GEN-LAST:event_btn_homeActionPerformed

    private void btn_surrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_surrActionPerformed
       // SUR WHEN ONLINE
        if(Mode == PLAYER_ONLINE){
           Client.requestSurrener();
           loadHome();
           Main.Mymain.playonline.setVisible(false);
        }
        else{
            JOptionPane.showMessageDialog(null, "Operation fail");
        }
        

    }//GEN-LAST:event_btn_surrActionPerformed

    public void checkResult(){
        if(BoardChess.checkResult() == 1){
                    String temp;
                    if(BoardChess.turn == Piece.White){
                        temp = "Chess Black Win";
                    }
                    else{
                        temp = "Chess White Win";
                    }
                            JOptionPane.showMessageDialog(new JFrame(), temp, "Notification",
                    JOptionPane.PLAIN_MESSAGE);
                  }
                else if(BoardChess.checkResult() == 2){
                    JOptionPane.showMessageDialog(new JFrame(),"ICE", "Notification",
                    JOptionPane.PLAIN_MESSAGE);
                }
    }
    
        
        
    public boolean Crown(){
        if(BoardChess.turn == Piece.White){
            for(int iCol = 0; iCol < 8; iCol++){
                if(BoardChess.chessBoard[0][iCol].getName().equals("White_Pawn")){
                    new Crown(new Coordinate(0, iCol), this).setVisible(true);
                    return true;
                }
            }
        }
        
        if(BoardChess.turn == Piece.Black){
            for(int iCol = 0; iCol < 8; iCol++){
                if(BoardChess.chessBoard[7][iCol].getName().equals("Black_Pawn")){
                    new Crown(new Coordinate(7, iCol), this).setVisible(true);
                    return true;
                }
            }
        }
        return false;
        
    }
    public void newGamePlayerAndPlayer(){
        chessBoard.removeAll();
        BoardChess.initValue();
        capacity = CHALLENGE;
        Mode = PLAYER_PLAYER; 
        if(BoardChess.turn == Piece.Black){
            lb_turn.setText("Turn of Black");
        }
        if(BoardChess.turn == Piece.White){
            lb_turn.setText("Turn of White");
        }
        chessBoard.add(BoardChess.paintChessBox(new EmptyCell(-1, new Coordinate(-1, -1)),null));
        chessBoard.setVisible(true);
        this.setVisible(true);
    }
    public void newGamePlayrtAndAI(){
        chessBoard.removeAll();
        BoardChess.initValue();
        BoardChess.initBoard(BoardChess.chessBoard);
        Mode = PLAYER_COMPUTER;
        waitAI = true;
        this.aI = new TheardAI(this,Piece.Black);
        JOptionPane.showMessageDialog(null, "You are a white player");
        chessBoard.add(BoardChess.paintChessBox(new EmptyCell(-1, new Coordinate(-1, -1)),null));
        chessBoard.setVisible(true);
        this.setVisible(true);
        
    }
    public void newGamePlayOnline(){
        chessBoard.removeAll();
        BoardChess.initValue();
        Mode = PLAYER_ONLINE;

        chessBoard.add(BoardChess.paintChessBox(new EmptyCell(-1, new Coordinate(-1, -1)),null));
        chessBoard.setVisible(true);
        this.setVisible(true);
    }
    public void loadHome(){
        BoardChess.initValue();
        chessBoard.removeAll();
        this.getContentPane().setBackground( Color.WHITE );
        ImageIcon icon = new ImageIcon("image/MAIN.jpg");
        JLabel label = new JLabel(); 
        label.setIcon(icon);
        chessBoard.add(label);
        chessBoard.setVisible(true);
        this.setVisible(true);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main().setVisible(true);
            }
        });
    }
    private javax.swing.JButton btn_home;
    private javax.swing.JButton btn_withComputer;
    private javax.swing.JButton btn_withPlayer;
    private javax.swing.JButton btn_training;
    private javax.swing.JButton btn_surr;
    public javax.swing.JPanel chessBoard;
    private javax.swing.JButton jButton4;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    public static javax.swing.JLabel lb_turn;
}

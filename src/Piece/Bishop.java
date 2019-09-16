/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Piece;

import Piece.Coordinate;
import Piece.Piece;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nthan
 */
public class Bishop {

	static int Side;
    static ArrayList arrMove;
    
    enum ChessSide
    {
        Black, White;
    }

    private static int[][] BishopTable = new int[][]
	{
      //9  8   7   6   5   4   3   2   1  0
       {0,   0,  0,  0,  0,  0,  0,  0,  0,   0}, //0

       {0,  -20,-10,-10,-10,-10,-10,-10,-20  ,0},
       {0,  -10,  0,  0,  0,  0,  0,  0,-10  ,0},
       {0,  -10,  0,  5, 10, 10,  5,  0,-10  ,0},
       {0,  -10,  5,  5, 10, 10,  5,  5,-10  ,0},
       {0,  -10,  0, 10, 10, 10, 10,  0,-10  ,0},
       {0,  -10, 10, 10, 10, 10, 10, 10,-10  ,0},            
       {0,  -10,  5,  0,  0,  0,  0,  5,-10  ,0},
       {0,  -20,-10,-40,-10,-10,-40,-10,-20  ,0},

       {0,   0,  0,  0,  0,  0,  0,  0,  0,   0}  //9
	};
	
	public static int GetPositionValue(Point pos, ChessSide eSide)
    {
        if (eSide == ChessSide.Black)
        {
            return BishopTable [pos.y][pos.x];
        }
        else
        {
            return BishopTable[9 - pos.y][9 - pos.x];
        }
    }
	
	public static ArrayList FindAllPossibleMove(int[][] State, Point pos)
    {

        arrMove = new ArrayList();

        Side = State[pos.x][pos.y] % 10;//Chẵn(0) là quân trắng, lẻ(1) là quân đen

        chessloop(State, pos, 1, 1);
        chessloop(State, pos, 1, -1);
        chessloop(State, pos, -1, -1);
        chessloop(State, pos, -1, 1);

        return arrMove;
    }
	
	static void chessloop(int[][] State, Point pos, int indexx, int indexy)
    {
        int stop = 0;
        int x = pos.x;
        int y = pos.y;
        while (stop == 0)
        {
            x += indexx;
            y += indexy;
            
            int state = State[x][y];
            if (state == 0)
            {
                arrMove.add(new Point(x, y));
            }
            else if (state == -1)
            {
                stop = 1;
            }
            else
            {
                if (state % 10 != Side)
                {
                    arrMove.add(new Point(x, y));
                }
                stop = 1;
            }

        }
    }
}

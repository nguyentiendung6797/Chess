package Piece;

public class Test {
	private static int[][] PawnTable = new int[][]
			{
        //9  8   7   6   5   4   3   2   1  0
         {0, 0,  0,  0,  0,  0,  0,  0,  0, 0}, //0
         {0, 0,  0,  0,  0,  0,  0,  0,  0, 0}, //1
         {0, 50, 50, 50, 50, 50, 50, 50, 50, 0},//2
         {0, 10, 10, 20, 30, 30, 20, 10, 10, 0},//3
         {0, 5,  5, 10, 27, 27, 10,  5,  5, 0}, //4
         {0,-5, -5,-10, 25, 25, -5, -5,  0, 0}, //5
         {0, 5, -5,-10,  0,  0,-10, -5,  5, 0}, //6
         {0, 5, 10, 10,-25,-25, 10, 10,  5, 0}, //7
         {0, 0,  0,  0,  0,  0,  0,  0,  0, 0}, //8
         {0, 0,  0,  0,  0,  0,  0,  0,  0, 0}  //9
			};		
		enum ChessSide
	    {
	        Black, White;
	    }
	
		public static int GetPositionValue(Point pos, ChessSide eSide)
        {
            int value = 0;
            if (eSide == ChessSide.Black)
            {

                value = PawnTable[pos.Y, pos.X];
                //Tốt cánh xe bị trừ 15% giá trị
                if (pos.X == 8 || pos.X == 1)
                    value -= 15;
            }
            else
            {

                value = PawnTable[9 - pos.Y, 9 - pos.X];
                if (pos.X == 8 || pos.X == 1)
                    value -= 15;
            }
            return value;
        }

}

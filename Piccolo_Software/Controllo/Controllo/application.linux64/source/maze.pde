int mazeRowCount = 4;
int mazeColCount = 4;

int pathWidth = 65;
int wallThickness = 8;
int paintDensity = 4;

int zeroX = 10;
int zeroY = 10;

Cell[] visitedCells = null;
int visitedCount = 0;
int cellCount = 0;

Cell map[][] = null;

void initialize()
{
  visitedCount = 0;
  cellCount = 0;

  cellCount = mazeColCount * mazeRowCount;
  visitedCells = new Cell[cellCount];

  Cell cell;
  map = new Cell[mazeColCount][mazeRowCount];

  for (int y = 0; y<mazeRowCount; y++)
    for ( int x=0; x<mazeColCount; x++)
    {
      cell = new Cell();
      cell.x = zeroX + x*pathWidth;
      cell.y = zeroY + y*pathWidth;
      cell.col = x;
      cell.row = y;
      map[x][y] = cell;
    }
}

void createMaze()
{
  initialize();
  initMaze(map[(int)random(mazeColCount)][(int)random(mazeRowCount)]);

  map[0][mazeRowCount-1].walls[Cell.BOTTOM] = Cell.NOWALL;
  map[mazeColCount-1][0].walls[Cell.TOP] = Cell.NOWALL;
}

void initMaze(Cell theCell)
{

  if ( visitedCount == cellCount)
    return;

  if (!isVisited(theCell))
  {
    theCell.visited = true;
    visitedCells[visitedCount++] = theCell;
  }

  int neibCount = 0;
  Cell[] neighbours = new Cell[4];

  Cell nextCell;
  if (theCell.col-1>=0 && !(nextCell = map[theCell.col-1][theCell.row]).visited)
  {
    neighbours[neibCount++] = nextCell;
  }
  if (theCell.row-1>=0 && !(nextCell=map[theCell.col][theCell.row-1]).visited)
  {
    neighbours[neibCount++] = nextCell;
  }
  if (theCell.col+1<this.mazeColCount && !(nextCell = map[theCell.col+1][theCell.row]).visited)
  {
    neighbours[neibCount++] = nextCell;
  }
  if (theCell.row+1<this.mazeRowCount && !(nextCell = map[theCell.col][theCell.row+1]).visited)
  {
    neighbours[neibCount++] = nextCell;
  }
  if (neibCount ==0)
  {
    initMaze(visitedCells[(int)random(visitedCount)]);
    return;
  }

  nextCell = neighbours[(int)random(neibCount)];

  if (nextCell.col < theCell.col)
  {
    nextCell.walls[Cell.RIGHT] = Cell.NOWALL;
    theCell.walls[Cell.LEFT] = Cell.NOWALL;
  }

  else if (nextCell.row < theCell.row)
  {
    nextCell.walls[Cell.BOTTOM] = Cell.NOWALL;
    theCell.walls[Cell.TOP] = Cell.NOWALL;
  }
  // neighbour right

  else if (nextCell.col > theCell.col)
  {
    nextCell.walls[Cell.LEFT] = Cell.NOWALL;
    theCell.walls[Cell.RIGHT] = Cell.NOWALL;
  }
  // neighbour bottom
  else if (nextCell.row > theCell.row)
  {
    nextCell.walls[Cell.TOP] = Cell.NOWALL;
    theCell.walls[Cell.BOTTOM] = Cell.NOWALL;
  }

  initMaze(nextCell);
}

boolean isVisited(Cell cell)
{
  if (visitedCount ==0)
    return false;

  for (int i=0; i<visitedCount; i++)
  {
    if (visitedCells[i] == cell)
      return true;
  }
  return false;
}

class Cell
{
  static final int LEFT = 0;
  static final int TOP = 1;
  static final int RIGHT = 2;
  static final int BOTTOM = 3;
  static final int NOWALL = 0;
  static final int HAVEWALL = 1;

  int state = 0;

  int x = 0;
  int y = 0;

  int col = 0;
  int row = 0;

  boolean visited = false;

  int[] walls = new int[] {
    HAVEWALL, HAVEWALL, HAVEWALL, HAVEWALL
  };

  void draw(PGraphics g)
  {
    int x1 = 0, y1=0, x2=0, y2=0;
    int x = this.x, y = this.y;
    for ( int i=0;i<4;i++)
    {
      if (walls[i] == NOWALL)
      {
        switch(i)
        {
          // left
        case LEFT:
          //          line(x, y+wallThickness, x+wallThickness, y+wallThickness);
          //          line(x, y+pathWidth-wallThickness, x+wallThickness, y+pathWidth-wallThickness);
          //            for(int k=0; k<pathWidth-2*wallThickness; k+=paintDensity)
          //            {
          //              g.line(x, y+wallThickness+k, x+wallThickness, y+wallThickness+k);
          //            }
          //            break;
          // top
        case TOP:
          //          line(x+wallThickness, y, x+wallThickness, y+wallThickness);
          //          line(x+pathWidth-wallThickness, y, x+pathWidth-wallThickness, y+wallThickness);
          //            for(int k=0;k<wallThickness; k+=paintDensity)
          //            {
          //               g.line(x+wallThickness, y+k, x+pathWidth-wallThickness, y+k);
          //            }
          break;
          // right
        case RIGHT:
          //          line(x+pathWidth-wallThickness, y+wallThickness, x+pathWidth, y+wallThickness);
          //          line(x+pathWidth-wallThickness, y+pathWidth-wallThickness, x+pathWidth, y+pathWidth-wallThickness);
          for (int k=0; k<2*wallThickness; k+=paintDensity/2)
          {
            g.line(x+pathWidth-wallThickness+k, y+wallThickness, x+pathWidth-wallThickness+k, y+pathWidth-wallThickness);
          }
          break;
          // bottom
        case BOTTOM:
          //          line(x+wallThickness, y+pathWidth-wallThickness, x+wallThickness, y+pathWidth);
          //          line(x+pathWidth-wallThickness, y+pathWidth-wallThickness, x+pathWidth-wallThickness, y+pathWidth);
          for (int k=0;k<wallThickness*2; k+=paintDensity/2)
          {
            g.line(x+wallThickness, y+pathWidth-wallThickness+k, x+pathWidth-wallThickness, y+pathWidth-wallThickness+k);
          }
          break;
        }
      }

      else if (walls[i] == HAVEWALL)
      {
        switch(i)
        {
          // left
        case LEFT:
          x1 = this.x;
          y1 = this.y;
          x2 = x1;
          y2 = y1 + pathWidth;
          // line(x+wallThickness, y+wallThickness, x+wallThickness, y+pathWidth-wallThickness);
          break;
          // top
        case TOP:
          x1 = this.x;
          y1 = this.y;
          x2 = x1 + pathWidth;
          y2 = y1;
          //line(x+wallThickness, y+wallThickness, x+pathWidth-wallThickness, y+wallThickness);
          break;
          // right
        case RIGHT:
          x1 = this.x+pathWidth;
          y1 = this.y;
          x2 = x1;
          y2 = y1 + pathWidth;
          //line(x+pathWidth-wallThickness, y+wallThickness, x+pathWidth-wallThickness, y+pathWidth-wallThickness);
          break;
          // bottom
        case BOTTOM:
          x1 = this.x;
          y1 = this.y +pathWidth;
          x2 = x1 +pathWidth;
          y2 = y1;
          //line(x+wallThickness, y+pathWidth-wallThickness, x+pathWidth-wallThickness, y+pathWidth-wallThickness);
          break;
        }

        //line(x1, y1, x2, y2);
        g.beginShape();
        for (int k=0; k<9; k+=1)
        {
          g.vertex(x+wallThickness+k*3, y+wallThickness+k*3);
          g.vertex(x+wallThickness+k*3, y+pathWidth-wallThickness-k*3);
          g.vertex(x+pathWidth-wallThickness-k*3, y+pathWidth-wallThickness-k*3);
          g.vertex(x+pathWidth-wallThickness-k*3, y+wallThickness+k*3);
          
        }
        g.endShape();
      }
    }
  }
}

void paintMaze(PGraphics g)
{
  for (int y=0; y<mazeRowCount; y++)
    for (int x=0; x<mazeColCount; x++)
      map[x][y].draw(g);

  int x = map[mazeColCount-1][0].x;
  int y = map[mazeColCount-1][0].y;
  for (int k=0;k<wallThickness; k+=paintDensity/2)
  {
    g.line(x+wallThickness, y+k, x+pathWidth-wallThickness, y+k);
  }
}


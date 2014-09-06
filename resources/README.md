Minesweeper
===========

Goal
----

Your goal is to implement an AI that plays [Minesweeper](https://en.wikipedia.org/wiki/Minesweeper_%28video_game%29).

Gameplay
--------

Minesweeper is played on a rectangular grid. Each cell in the grid may contain 
a mine (or it may not). All cells start out as closed, meaning that you don't 
know whether or not each contains a mine.

You play by opening one cell at a time. If the cell you opened contains a mine 
then — boom! — you lost. If not, it will reveal the number of its neighboring 
cells that contain mines. (Diagonal cells also count as neighbors, so in theory
there can be up to 8.)

If you open every mine-free cell on the grid, then congratulations, you've won!

Scoring
-------

TBD

Interface
---------

### Board dimensions

To find the dimensions of the minesweeper board that you'll be playing, make a 
GET request to `/info`. It will return this information in the form 
`width,height,mines`. So, for example, the return value `5,5,2` means we're 
playing on a 5×5 grid, with 2 mines.

### New game

To start a new game, make a POST request to `/new`. This will return a string 
that represents the ID of your new game. You'll need it to keep playing.

### Opening cells

To open a cell, make a POST request to `/open` with the following arguments:

- `id` the game ID you got when you created a new game
- `x` the column of the cell you want to open
- `y` the row of the cell you want to open

The coordinate of the _top-left_ cell is (0,0).

#### Response

- If you opened a cell with a mine, the return value will be the string "lost".
- If you opened the last mine-free cell, the return value will be the string "win".
- Otherwise, the return value will be the number of neighboring mines.

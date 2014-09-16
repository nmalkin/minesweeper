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

Submissions will be ranked by their winning percentage (i.e., the fraction of 
games the AI has won). Only submissions with 10,000 games or more will be 
ranked.

Interface
---------

A server with a web API will be managing the games, but __we recommend using__ 
[our helper client](https://github.com/nmalkin/minesweeper-client#minesweeper-client) 
to save time. Please follow that link for further instructions.

Web API
-------

This is the API for the game. (Again, we recommend using the helper client 
instead of accessing the HTTP API directly.)

### Board dimensions

To find the dimensions of the minesweeper board that you'll be playing, make a 
GET request to `/info`. It will return this information in the form 
`width,height,mines`. So, for example, the return value `5,5,2` means we're 
playing on a 5×5 grid, with 2 mines.

### New game

To start a new game, make a POST request to `/new`. You will need to include 
your `name` as a field in the request. In return, you'll get a string 
that represents the ID of your new game. You'll need it to keep playing.

You can also include a `version` number as part of your request (integers only, 
please). These differentiate your submissions: only games with the highest 
version number will be counted for scoring.

### Opening cells

To open a cell, make a POST request to `/open` with the following arguments:

- `id` the game ID you got when you created a new game
- `x` the column of the cell you want to open
- `y` the row of the cell you want to open

The coordinate of the _top-left_ cell is (0,0).

#### Response

- If you opened a cell with a mine, the return value will be the string `lost`.
- If you opened the last mine-free cell, the return value will be the string `win`.
- Otherwise, the return value will be the number of neighboring mines.

#### Game walkthrough using cURL

To get a sense for what gameplay feels like, let's walk through an example game 
using cURL.

Before we start playing, we should check what is the size of the grid we are dealing with:

```
$ curl minesweeper.nm.io/info; echo
5,5,2
```

Now that we know we are playing a 5 x 5 grid with 2 mines, let's start a new game called `newgame`

```
$ curl --data "name=newgame" minesweeper.nm.io/new; echo
48088a80-85c9-43c8-892f-80d11c22d2d5
```

With this id (`48088a80-85c9-43c8-892f-80d11c22d2d5`) we can start playing our game. Let's try to see if there is a mine at `(0,0)`.

```
curl -X POST --data "id=0ed84c7e-b296-488f-8ecc-aaf4cebf7086&x=0&y=0" minesweeper.nm.io/open; echo
0
```

The return was 0, which means that there are `0` mines around `(0,0)`. Cool, the next two steps should be obvious: open `(0,1)(1,0)(1,1)`:

```
$ curl -X POST --data "id=0ed84c7e-b296-488f-8ecc-aaf4cebf7086&x=0&y=1" minesweeper.nm.io/open; echo
0
$ curl -X POST --data "id=0ed84c7e-b296-488f-8ecc-aaf4cebf7086&x=1&y=0" minesweeper.nm.io/open; echo
1
$ curl -X POST --data "id=0ed84c7e-b296-488f-8ecc-aaf4cebf7086&x=1&y=1" minesweeper.nm.io/open; echo
1
```

Seems like there is a Mine lurking around somewhere. Our board must look like this:

    #######
    #01???#
    #01???#
    #?????#
    #?????#
    #?????#
    #######

The coordinates `(0,2) and (1,2)` seem safe. Let's open those:

```
$ curl -X POST --data "id=0ed84c7e-b296-488f-8ecc-aaf4cebf7086&x=0&y=2" minesweeper.nm.io/open; echo
0
$ curl -X POST --data "id=0ed84c7e-b296-488f-8ecc-aaf4cebf7086&x=1&y=2" minesweeper.nm.io/open; echo
0
```

With this info, it becomes really obvious identifying where the mine is `(2,1)`, and walking around it:

    #######
    #01X10#
    #01110#
    #00???#
    #?????#
    #?????#
    #######

So now we can just continue opening slots that we know are safe, and finish the game:


    #######
    #01X10#
    #01110#
    #00000#
    #00011#
    #0001X#
    #######
    

After we find that in this particular game the last mine was at `(4,4)`, we will get:

```
$ curl -X POST --data "id=0ed84c7e-b296-488f-8ecc-aaf4cebf7086&x=3&y=4" minesweeper.nm.io/open; echo
win
```

Congrats, you just won this game!

package player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import ship.Ship;
import world.World;

/**
 * Probabilistic guess player (task C). Please implement this class.
 *
 * @author Youhan Xia, Jeffrey Chan
 */
public class ProbabilisticGuessPlayer implements Player
{

    private World world;
    private HashMap<String, Status[][]> locMap;
    private ArrayList<World.ShipLocation> remShips = new ArrayList<World.ShipLocation>();
    private ArrayList<World.ShipLocation> remShipsCpy = new ArrayList<World.ShipLocation>();

    private ArrayList<Ship> oppoShips = new ArrayList<Ship>();
    private Status[][] currBrdState, currShipState; // checks if all hit
    private ArrayList<World.Coordinate> boardCrdnts = new ArrayList<World.Coordinate>();
    private ArrayList<Guess> previousG = new ArrayList<Guess>();
    private ArrayList<Guess> parityDat = new ArrayList<Guess>();
    private ArrayList<Guess> verticalCrdnts = new ArrayList<Guess>();

    private Random rnd = new Random();

    @Override
    public void initialisePlayer(World world)
    {
        boardCrdnts = new ArrayList<World.Coordinate>();
        previousG = new ArrayList<Guess>();
        this.world = world;
        locMap = new HashMap<String, Status[][]>();
        this.parityDat = new ArrayList<Guess>();
        this.verticalCrdnts = new ArrayList<Guess>();
        currBrdState = new Status[world.numRow][world.numColumn];

        remShips = world.shipLocations;
        remShipsCpy = (ArrayList<World.ShipLocation>) remShips.clone();

        // List of opponent ships
        oppoShips = new ArrayList<Ship>();
        for (int i = 0; i < this.remShipsCpy.size(); i++)
        {
            oppoShips.add(remShipsCpy.get(i).ship);
        }

        // world Coordinate creation
        for (int x = 0; x < world.numRow; x++)
        {
            for (int y = 0; y < world.numColumn; y++)
            {
                Status status = new Status();
                status.isShot = false;
                status.count = 0;
                this.currBrdState[x][y] = status;

                for (int i = 0; i < remShips.size(); i++)
                {
                    String shipName = remShips.get(i).ship.name();
                    currShipState = locMap.get(shipName);
                    status = new Status();
                    status.isShot = false;
                    status.count = 0;
                    if (currShipState == null)
                    {
                        currShipState = new Status[world.numRow][world.numColumn];
                        locMap.put(shipName, currShipState);
                    }
                    currShipState[x][y] = status;
                }
            }
        }
        this.calculatePosibilityCount();
    } // end of initialisePlayer()

    @Override
    public Answer getAnswer(Guess guess)
    {
        Answer answer = new Answer();
        ArrayList<World.Coordinate> coordinateList;
        World.Coordinate coordinate;
        Ship ship;
        for (int i = 0; i < this.remShips.size(); i++)
        {
            coordinateList = this.remShips.get(i).coordinates;
            ship = this.remShips.get(i).ship;
            for (int j = 0; j < coordinateList.size(); j++)
            {
                coordinate = coordinateList.get(j);
                if (coordinate.row == guess.row && coordinate.column == guess.column)
                {
                    answer.isHit = true;
                    coordinateList.remove(j);
                    if (coordinateList.isEmpty())
                    { // current ship is sunk
                        answer.shipSunk = ship;
                        this.remShips.remove(i);
                        return answer;
                    }
                }
            }
        }

        return answer;
    } // end of getAnswer()

    @Override
    public Guess makeGuess()
    {
        // Cell with highest total count
        int highestCount = 0;
        World.Coordinate coordinate;
        Status status = null;
        Guess g1, g2;
        int xCoordinate, yCoordinate, newXCoordinate, newYCoordinate;

        coordinate = this.world.new Coordinate();

        // hunting mode
        if (previousG.size() > 1)
        {
            Guess previousG1, previousG2;
            previousG1 = previousG.get(0);
            previousG2 = previousG.get(previousG.size() - 1);

            // fire east direction
            if (previousG1.row < previousG2.row)
            {
                coordinate = this.world.new Coordinate();
                coordinate.row = previousG2.row;
                coordinate.column = previousG2.column;
                g2 = this.fireToEast(coordinate);
                if (g2 != null)
                {
                    return g2;
                }
                // fire opposite direction
                coordinate.row = previousG1.row;
                coordinate.column = previousG1.column;
                g2 = this.fireToWest(coordinate);

                if (g2 != null)
                {
                    return g2;
                }
            }
            // fire west direction
            else if (previousG1.row > previousG2.row)
            {
                coordinate = this.world.new Coordinate();
                coordinate.row = previousG2.row;
                coordinate.column = previousG2.column;
                g2 = this.fireToWest(coordinate);
                if (g2 != null)
                {
                    return g2;
                }

                coordinate.row = previousG1.row;
                coordinate.column = previousG1.column;
                g2 = this.fireToEast(coordinate);

                if (g2 != null)
                {
                    return g2;
                }
            }
            // fire north direction
            else if (previousG1.column > previousG2.column)
            {
                coordinate = this.world.new Coordinate();
                coordinate.row = previousG2.row;
                coordinate.column = previousG2.column;
                g2 = this.fireToNorth(coordinate);
                if (g2 != null)
                {
                    return g2;
                }

                coordinate.row = previousG1.row;
                coordinate.column = previousG1.column;
                g2 = this.fireToSouth(coordinate);

                if (g2 != null)
                {
                    return g2;
                }
            }
            // fire south direction
            else if (previousG1.column < previousG2.column)
            {
                coordinate = this.world.new Coordinate();
                coordinate.row = previousG2.row;
                coordinate.column = previousG2.column;
                g2 = this.fireToSouth(coordinate);
                if (g2 != null)
                {
                    return g2;
                }

                coordinate.row = previousG1.row;
                coordinate.column = previousG1.column;
                g2 = this.fireToNorth(coordinate);

                if (g2 != null)
                {
                    return g2;
                }
            }

            // Previous guesses copies to parity data
            for (int i = 0; i < previousG.size(); i++)
            {
                this.parityDat.add(previousG.get(i));
            }
            this.previousG.clear();
            this.previousG.add(this.parityDat.get(0));
            this.parityDat.remove(0);
        }

        // hit shipt wiht previous guess
        if (this.previousG.size() == 1)
        {
            // finds direction to hit
            g1 = previousG.get(0);
            xCoordinate = g1.row;
            yCoordinate = g1.column;

            // check east direction
            newXCoordinate = xCoordinate + 1;
            if (newXCoordinate < this.world.numRow)
            {
                if (!this.currBrdState[newXCoordinate][yCoordinate].isShot)
                {
                    status = this.currBrdState[newXCoordinate][yCoordinate];
                    coordinate.row = newXCoordinate;
                    coordinate.column = yCoordinate;
                }
            }
            newXCoordinate = xCoordinate;

            // check south direction
            newYCoordinate = yCoordinate + 1;
            if (newYCoordinate < this.world.numColumn)
            {
                if (!this.currBrdState[xCoordinate][newYCoordinate].isShot)
                {
                    if (status == null)
                    {
                        if (!this.currBrdState[xCoordinate][newYCoordinate].isShot)
                        {
                            status = this.currBrdState[xCoordinate][newYCoordinate];
                            coordinate.row = xCoordinate;
                            coordinate.column = newYCoordinate;
                        }
                    }
                    if (status.count < this.currBrdState[xCoordinate][newYCoordinate].count)
                    {
                        status = this.currBrdState[xCoordinate][newYCoordinate];
                        coordinate.row = xCoordinate;
                        coordinate.column = newYCoordinate;
                    }
                }
            }
            newYCoordinate = yCoordinate;

            // check west direction
            newXCoordinate = xCoordinate - 1;
            if (newXCoordinate >= 0)
            {
                if (!this.currBrdState[newXCoordinate][yCoordinate].isShot)
                {
                    if (status == null)
                    {
                        status = this.currBrdState[newXCoordinate][yCoordinate];
                        coordinate.row = newXCoordinate;
                        coordinate.column = yCoordinate;
                    } else if (status.count < this.currBrdState[newXCoordinate][yCoordinate].count)
                    {
                        status = this.currBrdState[newXCoordinate][yCoordinate];
                        coordinate.row = newXCoordinate;
                        coordinate.column = yCoordinate;
                    }
                }
            }
            newXCoordinate = xCoordinate;

            // check north direction
            newYCoordinate = yCoordinate - 1;
            if (newYCoordinate >= 0)
            {
                if (!this.currBrdState[xCoordinate][newYCoordinate].isShot)
                {
                    if (status == null)
                    {
                        status = this.currBrdState[xCoordinate][newYCoordinate];
                        coordinate.row = xCoordinate;
                        coordinate.column = newYCoordinate;
                    } else if (status.count < this.currBrdState[xCoordinate][newYCoordinate].count)
                    {
                        status = this.currBrdState[xCoordinate][newYCoordinate];
                        coordinate.row = xCoordinate;
                        coordinate.column = newYCoordinate;
                    }
                }
            }

            if (status != null)
            {
                g2 = new Guess();
                g2.column = coordinate.column;
                g2.row = coordinate.row;

                status.isShot = true;
                status.count = 0;
                return g2;
            }

        }

        if (previousG.size() > 0)
        {
            previousG.clear();
        }

        this.calculatePosibilityCount();
        // no hunting history
        for (int x = 0; x < this.world.numRow; x++)
        {
            for (int y = 0; y < this.world.numColumn; y++)
            {

                if (this.currBrdState[x][y].count == 0)
                {
                    continue;
                }

                if (this.currBrdState[x][y].count > highestCount)
                {
                    boardCrdnts.clear();
                    coordinate = this.world.new Coordinate();
                    coordinate.row = x;
                    coordinate.column = y;
                    boardCrdnts.add(coordinate);
                    highestCount = this.currBrdState[x][y].count;
                } else if (this.currBrdState[x][y].count == highestCount)
                {
                    coordinate = this.world.new Coordinate();
                    coordinate.row = x;
                    coordinate.column = y;
                    boardCrdnts.add(coordinate);
                } else
                {
                    continue;
                }
            }
        }

        if (boardCrdnts.isEmpty())
        {
            return null;
        }

        // randomly choose a highest coordinate
        g2 = new Guess();
        coordinate = boardCrdnts.get(rnd.nextInt(boardCrdnts.size()));

        g2.column = coordinate.column;
        g2.row = coordinate.row;

        status = this.currBrdState[coordinate.row][coordinate.column];
        status.count = 0;
        status.isShot = true;

        boardCrdnts.clear();
        return g2;
    } // end of makeGuess()

    @Override
    public void update(Guess newG, Answer newAnswer)
    {

        if (newAnswer.shipSunk == null)
        {
            if (newAnswer.isHit == true)
            {
                this.previousG.add(newG);
            }
        } else
        {
            Ship opptShip;
            // remove destroyed ship
            for (int i = 0; i < this.oppoShips.size(); i++)
            {
                opptShip = this.oppoShips.get(i);
                if (opptShip.name().equals(newAnswer.shipSunk.name()) == true)
                {
                    this.oppoShips.remove(i);
                }
            }

            // remove co-ordinates for destroyed ship
            if (newG.row < this.previousG.get(0).row)
            {
                count: for (int a = 1; a < newAnswer.shipSunk.len(); a++)
                {
                    for (int g = 0; g < this.previousG.size(); g++)
                    {
                        if ((newG.row + a) == previousG.get(g).row)
                        {
                            previousG.remove(g);
                            continue count;
                        }
                    }
                }
            } else if (newG.row > this.previousG.get(0).row)
            {
                count: for (int a = 1; a < newAnswer.shipSunk.len(); a++)
                {
                    for (int g = 0; g < this.previousG.size(); g++)
                    {
                        if ((newG.row - a) == previousG.get(g).row)
                        {
                            previousG.remove(g);
                            continue count;
                        }
                    }
                }
            } else if (newG.column < this.previousG.get(0).column)
            {
                count: for (int a = 1; a < newAnswer.shipSunk.len(); a++)
                {
                    for (int g = 0; g < this.previousG.size(); g++)
                    {
                        if ((newG.column + a) == this.previousG.get(g).column)
                        {
                            previousG.remove(g);
                            continue count;
                        }
                    }
                }
            } else if (newG.column > this.previousG.get(0).column)
            {
                count: for (int a = 1; a < newAnswer.shipSunk.len(); a++)
                {
                    for (int g = 0; g < this.previousG.size(); g++)
                    {
                        if ((newG.column - a) == this.previousG.get(g).column)
                        {
                            previousG.remove(g);
                            continue count;
                        }
                    }
                }
            }

            // copy vertical ship coordinate
            if (previousG.size() > 0)
            {
                for (Guess copy : this.previousG)
                {
                    this.verticalCrdnts.add(copy);
                }
                this.previousG.clear();
            }

            if (this.verticalCrdnts.size() > 0)
            {
                previousG.add(this.verticalCrdnts.get(0));
                this.verticalCrdnts.remove(0);
            } else if (this.parityDat.size() > 0)
            {
                this.previousG.add(this.parityDat.get(0));
                this.parityDat.remove(0);
            }
        }
        this.calculatePosibilityCount();
    } // end of update()

    @Override
    public boolean noRemainingShips()
    {
        return this.remShips.isEmpty();
    } // end of noRemainingShips()

    private void resetBoardDatas()
    {
        for (int m = 0; m < this.world.numRow; m++)
        {
            for (int n = 0; n < this.world.numColumn; n++)
            {
                this.currBrdState[m][n].count = 0;
            }
        }
    }

    private void calculatePosibilityCount()
    {
        Status status;
        ArrayList<Status> tempAl = new ArrayList<Status>();
        ArrayList<Status> finalAl = new ArrayList<Status>();
        Ship targetShip;

        this.resetBoardDatas();

        for (int i = 0; i < this.oppoShips.size(); i++)
        {
            targetShip = this.oppoShips.get(i);
            String targetShipName = targetShip.name();
            currShipState = this.locMap.get(targetShipName);
            for (int x = 0; x < this.world.numRow; x++)
            {
                for (int y = 0; y < this.world.numColumn; y++)
                {
                    status = this.currBrdState[x][y];
                    // check status of target coord
                    if (!status.isShot)
                    {
                        finalAl.add(status);
                        // check direction horizontally
                        for (int l = 1; l < targetShip.len(); l++)
                        {
                            // move east
                            if (x + l < world.numRow)
                            {
                                if (currBrdState[x + l][y].isShot)
                                {
                                    tempAl.clear();
                                    break;
                                }
                                tempAl.add(this.currBrdState[x + l][y]);
                            } else
                            {
                                tempAl.clear();
                                break;
                            }
                        }

                        // update arreaylist
                        if (tempAl.size() > 0)
                        {
                            for (int s = 0; s < tempAl.size(); s++)
                            {
                                finalAl.add(tempAl.get(s));
                            }
                            tempAl.clear();
                        }

                        // check east
                        for (int l = 1; l < targetShip.len(); l++)
                        {
                            if (x - l >= 0)
                            {
                                if (currBrdState[x - l][y].isShot)
                                {
                                    tempAl.clear();
                                    break;
                                }
                                tempAl.add(currBrdState[x - l][y]);
                            } else
                            {
                                tempAl.clear();
                                break;
                            }
                        }

                        if (tempAl.size() > 0)
                        {
                            for (int s = 0; s < tempAl.size(); s++)
                            {
                                finalAl.add(tempAl.get(s));
                            }
                            tempAl.clear();
                        }

                        // check south
                        for (int l = 1; l < targetShip.len(); l++)
                        {
                            if (y + l < this.world.numColumn)
                            {
                                if (currBrdState[x][y + l].isShot)
                                {
                                    tempAl.clear();
                                    break;
                                }
                                tempAl.add(currBrdState[x][y + l]);
                            } else
                            {
                                tempAl.clear();
                                break;
                            }
                        }

                        if (tempAl.size() > 0)
                        {
                            for (int s = 0; s < tempAl.size(); s++)
                            {
                                finalAl.add(tempAl.get(s));
                            }
                            tempAl.clear();
                        }

                        // check south
                        for (int s = 1; s < targetShip.len(); s++)
                        {
                            if (y - s >= 0)
                            {
                                if (currBrdState[x][y - s].isShot)
                                {
                                    tempAl.clear();
                                    break;
                                }
                                tempAl.add(currBrdState[x][y - s]);
                            } else
                            {
                                tempAl.clear();
                                break;
                            }
                        }

                        if (tempAl.size() > 0)
                        {
                            for (int s = 0; s < tempAl.size(); s++)
                            {
                                finalAl.add(tempAl.get(s));
                            }
                            tempAl.clear();
                        }
                    }
                }
            }

            if (finalAl.size() > 0)
            {
                for (int s = 0; s < finalAl.size(); s++)
                {
                    finalAl.get(s).count++;
                }
                finalAl.clear();
            }
        }
    }

    private Guess fireToEast(World.Coordinate startCdn)
    {
        Status status;
        if (startCdn.row + 1 < this.world.numRow)
        {
            if (!this.currBrdState[startCdn.row + 1][startCdn.column].isShot)
            {
                Guess guess = new Guess();
                guess.row = startCdn.row + 1;
                guess.column = startCdn.column;
                status = this.currBrdState[guess.row][guess.column];
                status.count = 0;
                status.isShot = true;
                return guess;
            }
        }
        return null;
    }

    private Guess fireToWest(World.Coordinate startCdn)
    {
        Status status;
        if (startCdn.row - 1 >= 0)
        {
            if (!this.currBrdState[startCdn.row - 1][startCdn.column].isShot)
            {
                Guess guess = new Guess();
                guess.row = startCdn.row - 1;
                guess.column = startCdn.column;
                status = this.currBrdState[guess.row][guess.column];
                status.count = 0;
                status.isShot = true;
                return guess;
            }
        }
        return null;
    }

    private Guess fireToNorth(World.Coordinate startCdn)
    {
        Status status;
        if (startCdn.column - 1 >= 0)
        {
            if (!this.currBrdState[startCdn.row][startCdn.column - 1].isShot)
            {
                Guess guess = new Guess();
                guess.row = startCdn.row;
                guess.column = startCdn.column - 1;
                status = this.currBrdState[guess.row][guess.column];
                status.count = 0;
                status.isShot = true;
                return guess;
            }
        }
        return null;
    }

    private Guess fireToSouth(World.Coordinate startCdn)
    {
        Status status;
        if (startCdn.column + 1 < this.world.numColumn)
        {
            if (!this.currBrdState[startCdn.row][startCdn.column + 1].isShot)
            {
                Guess guess = new Guess();
                guess.row = startCdn.row;
                guess.column = startCdn.column + 1;
                status = this.currBrdState[guess.row][guess.column];
                status.count = 0;
                status.isShot = true;
                return guess;
            }
        }
        return null;
    }

    class Status
    {
        boolean isShot;
        int count;
    }
} // end of class ProbabilisticGuessPlayer
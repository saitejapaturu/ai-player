package player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;

import ship.Ship;
import world.World;
import world.World.Coordinate;
import world.World.ShipLocation;

/**
 * Greedy guess player (task B). Please implement this class.
 *
 * @author Youhan Xia, Jeffrey Chan
 */
public class GreedyGuessPlayer implements Player
{

    World world;
    ArrayList<World.ShipLocation> remShips = new ArrayList<>();
    ArrayList<Guess> g1 = new ArrayList<Guess>(); // using parity principle
    ArrayList<Guess> g2 = new ArrayList<Guess>(); // hunting guess
    Stack<Guess> prevShot = new Stack<Guess>();
    Stack<Guess> hits = new Stack<Guess>();
    Guess lastHit = new Guess();
    Guess previousHit = new Guess();
    boolean targetingMode = false;
    boolean swapped = false;

    @Override
    public void initialisePlayer(World world)
    {
        this.world = world;

        // insert ships
        for (int i = 0; i < world.shipLocations.size(); i++)
        {
            remShips.add(world.shipLocations.get(i));
        }

        // guess1 generation
        // uses principle parity
        for (int row = 0; row < world.numRow; row = row + 2)
        {
            for (int col = 0; col < world.numColumn; col = col + 2)
            {
                Guess parityGuess = new Guess();
                parityGuess.row = row;
                parityGuess.column = col;

                g1.add(parityGuess);
            }
        }
        // removing odd cells
        for (int row = 1; row < world.numRow; row = row + 2)
        {
            for (int col = 1; col < world.numColumn; col = col + 2)
            {
                Guess oddGuess = new Guess();
                oddGuess.row = row;
                oddGuess.column = col;

                g1.add(oddGuess);
            }
        }
    } // end of initialisePlayer()

    @Override
    public Answer getAnswer(Guess guess)
    {
        Answer ans = new Answer();
        Coordinate targetPos;
        // check guess targetPos with ship targetPos.
        // compare guess targetPos and ship targetPos
        for (int i = 0; i < remShips.size(); i++)
        {
            ShipLocation possibleShip = remShips.get(i);
            Iterator<Coordinate> iter = possibleShip.coordinates.iterator();
            while (iter.hasNext())
            {
                targetPos = iter.next();
                // check for hit
                if (guess.column == targetPos.column && guess.row == targetPos.row)
                {
                    ans.isHit = true;
                    iter.remove(); // coordinate removed from the ship

                    if (possibleShip.coordinates.isEmpty())
                    {
                        ans.shipSunk = possibleShip.ship; // shipSunk set
                        this.remShips.remove(possibleShip); // remove ship
                        return ans;
                    }
                }
            }
        }

        return ans;
    } // end of getAnswer()

    @Override
    public Guess makeGuess()
    {

        Guess newG = new Guess();
        Random random = new Random();
        int index;
        boolean validShot = false;

        if (targetingMode == true)
        {
            if (!g2.isEmpty())
            { // hunting guess
                do
                {
                    // random cell number
                    index = random.nextInt(g2.size());
                    newG = g2.get(index);
                    if (isValidShot(newG))
                    {
                        g2.remove(index);
                        return newG;
                    }
                } while (validShot == false);
            }

        } else
        {
            if (g1.size() != 0)
            {
                do
                {
                    // random cell number
                    index = random.nextInt(g1.size());
                    newG = g1.get(index);
                    if (isValidShot(newG))
                    {
                        g1.remove(index);
                        return newG;
                    }
                } while (validShot == false);
            }
        }
        return newG;
    } // end of makeGuess()

    @Override
    public void update(Guess guess, Answer answer)
    {

        prevShot.push(guess);

        if (answer.isHit == true)
        {
            hits.push(guess);
            targetingMode = true;
            targetAdjacentGuesses(guess);
        }

        if (targetingMode == true)
        {
            if (g2.isEmpty())
            {
                targetingMode = false;
            } else if (answer.isHit == false)
            {
                hits = reverseStack(hits);
            }
        }
    } // end of update()

    @Override
    public boolean noRemainingShips()
    {
        return remShips.isEmpty();
    } // end of noRemainingShips()

    // target adjacent guess after hit
    public void targetAdjacentGuesses(Guess guess)
    {
        Guess north = new Guess();
        north.column = guess.column;
        north.row = guess.row + 1;

        Guess south = new Guess();
        south.column = guess.column;
        south.row = guess.row - 1;

        Guess west = new Guess();
        west.column = guess.column - 1;
        west.row = guess.row;

        Guess east = new Guess();
        east.column = guess.column + 1;
        east.row = guess.row;

        if (isValidShot(west) && isValidGuess(west))
            g2.add(west);
        if (isValidShot(south) && isValidGuess(south))
            g2.add(south);
        if (isValidShot(north) && isValidGuess(north))
            g2.add(north);
        if (isValidShot(east) && isValidGuess(east))
            g2.add(east);
    }

    private Stack<Guess> reverseStack(Stack<Guess> stack)
    {
        Stack<Guess> temporary = new Stack<Guess>();

        while (!stack.isEmpty())
        {
            temporary.push(stack.pop());
        }
        return temporary;
    }

    // checks if guess is valid
    public boolean isValidGuess(Guess guess)
    {
        Guess test = new Guess();
        Iterator<Guess> iter = g2.iterator();

        // used cell
        while (iter.hasNext())
        {
            test = iter.next();
            if (test.row == guess.row && test.column == guess.column)
            {
                return false;
            }
        }
        return true;
    }

    // checks if shot is valid
    public boolean isValidShot(Guess guess)
    {
        Guess check = new Guess();
        Iterator<Guess> iter = prevShot.iterator();

        // cell used
        while (iter.hasNext())
        {
            check = iter.next();
            if (check.column == guess.column && check.row == guess.row)
            {
                return false;
            }
        }

        if (guess.column < 0 || guess.column >= world.numColumn || guess.row < 0 || guess.row >= world.numRow)
        {
            return false;
        }

        return true;
    }
} // end of class GreedyGuessPlayer

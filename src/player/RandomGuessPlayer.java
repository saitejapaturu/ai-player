package player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import world.World;
import world.World.Coordinate;
import world.World.ShipLocation;

/**
 * Random guess player (task A). Please implement this class.
 *
 * @author Youhan Xia, Jeffrey Chan
 */
public class RandomGuessPlayer implements Player
{

    ArrayList<ShipLocation> remainingShips = new ArrayList<>();
    ArrayList<Guess> g1 = new ArrayList<>(); // random guess
    ArrayList<Guess> g2 = new ArrayList<>(); // previous guess
    World world;

    @Override
    public void initialisePlayer(World world)
    {
        this.world = world;

        // introducing the ships to the board
        for (int i = 0; i < world.shipLocations.size(); i++)
        {
            remainingShips.add(world.shipLocations.get(i));
        }

        // Random guesses generation
        for (int row = 0; row < world.numRow; row++)
        {
            for (int col = 0; col < world.numColumn; col++)
            {
                Guess rndGuess = new Guess();
                rndGuess.row = row;
                rndGuess.column = col;

                g1.add(rndGuess);
            }
        }
    } // end of initialisePlayer()

    @Override
    public Answer getAnswer(Guess guess)
    {
        Answer ans = new Answer();
        Coordinate crdnts;

        // compare guess and ship coordinates
        for (int i = 0; i < remainingShips.size(); i++)
        {
            ShipLocation posbShip = remainingShips.get(i);
            Iterator<Coordinate> iter = posbShip.coordinates.iterator();
            while (iter.hasNext())
            {
                crdnts = iter.next();

                // Check hit
                if (guess.column == crdnts.column && guess.row == crdnts.row)
                {
                    ans.isHit = true;
                    iter.remove(); // Remove the coordinate from the ship

                    if (posbShip.coordinates.isEmpty())
                    {
                        ans.shipSunk = posbShip.ship; // shipSunk set
                        this.remainingShips.remove(posbShip); // remove the ship
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

        Guess nGuess = new Guess();
        Random rnd = new Random();
        int index;
        boolean shotValid = false;

        if (!g1.isEmpty())
        {
            do
            {
                // Random cell number acquired
                index = rnd.nextInt(g1.size());
                nGuess = g1.get(index);
                if (isValidShot(nGuess))
                {
                    g1.remove(index);
                    return nGuess;
                }

            } while (!shotValid);
        }

        return nGuess;
    } // end of makeGuess()

    @Override
    public void update(Guess guess, Answer answer)
    {
        // Not needed for random guess
    } // end of update()

    @Override
    public boolean noRemainingShips()
    {
        return remainingShips.isEmpty();
    } // end of noRemainingShips()

    // checks if shot is valid
    public boolean isValidShot(Guess guess)
    {
        Iterator<Guess> iter = g2.iterator();
        Guess check = new Guess();

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
} // end of class RandomGuessPlayer

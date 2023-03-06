package il.co.payturn.omry.payturn;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class SwipeAdapter extends FragmentStatePagerAdapter {

    public SwipeAdapter(FragmentManager fm) {
        super(fm);
    }

    /**
     * getItem - returns the Fragment associated with the specified position.
     * @param position - the position of the Fragment
     * @return if the fragment position is 0, the user will be directed to the Debts fragment,
     * if the position is 1, the user will be directed to the Debits fragment,
     * and if the position is not any of these numbers, the method will just return null.
     */
    @Override
    public Fragment getItem(int position) {
        switch (position){
            //Here are the fragment files for swipe
            case 0 : return new DebtsFragment();
            case 1 : return new DebitsFragment();
        }
        return null;
    }

    /**
     * getCount - this method just returns the number of Fragments we have in the whole adapter, which is 2 (DebtsFragment & DebitsFragment).
     * @return - the number of fragments
     */
    @Override
    public int getCount() {
        return 2;
    }
}

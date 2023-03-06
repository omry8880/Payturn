package il.co.payturn.omry.payturn;

import android.support.annotation.NonNull;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Debt implements Comparable<Debt> {
    private String ID;
    private String name;
    private double sum;
    private String deadline;
    private String addedDate;
    private String debtOwner_ID; // The one who has the debt
    private String debtCollector_ID; // The one who collects the debt
    private String image;
    private boolean status; // Notification

    public Debt(String ID, String name, double sum, String deadline, String addedDate, String debtOwner_ID, String debtCollector_ID, String image, boolean status) {
        this.ID = ID;
        this.name = name;
        this.sum = sum;
        this.deadline = deadline;
        this.addedDate = addedDate;
        this.debtOwner_ID = debtOwner_ID;
        this.debtCollector_ID = debtCollector_ID;
        this.image = image;
        this.status = status;
    }

    public Debt() {

    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) { this.deadline = deadline; }

    public String getAddedDate() { return addedDate; }

    public void setAddedDate(String addedDate) { this.addedDate = addedDate; }

    public String getDebtOwner_ID() {
        return debtOwner_ID;
    }

    public void setDebtOwner_ID(String debtOwner_ID) {
        this.debtOwner_ID = debtOwner_ID;
    }

    public String getDebtCollector_ID() {
        return debtCollector_ID;
    }

    public void setDebtCollector_ID(String debtCollector_ID) {
        this.debtCollector_ID = debtCollector_ID;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    /**
     * compareTo - this function is used when we want to sort an object (for example Arraylist of Debts) in a certain order
     * In this case, the order will be determined by the date in which a debt was created (day,month,year, hours,minutes,seconds)
     * @param o - the selected Debt object
     * @return - returns 0 if the dates are the same, returns 1 if a certain date is before another date, returns -1 if another date comes before a certain date
     */
    @Override
    public int compareTo(@NonNull Debt o) { // Makes the listview sort order by time of creation.
        ParsePosition pos1 = new ParsePosition(0);
        SimpleDateFormat sdf1 = new SimpleDateFormat("d/M/yyyy HH:mm:ss");
        Date date1 = sdf1.parse(o.addedDate, pos1);

        ParsePosition pos2 = new ParsePosition(0);
        SimpleDateFormat sdf2 = new SimpleDateFormat("d/M/yyyy HH:mm:ss");
        Date date2 = (sdf2).parse(this.addedDate, pos2);

        if (date2.equals(date1)) {
            return 0;
        }

        if (date2.before(date1)) {
            return 1;
        }

        return -1;
    }
}

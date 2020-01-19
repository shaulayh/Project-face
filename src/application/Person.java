package application;


/**
 * @author Azeez G. Shola
 * @version 1.0
 */
public class Person {
    /**
     * code of the face
     */
    private int code;

    /**
     * first name of person
     */
    private String firstName;

    /**
     * last name of the person
     */
    private String lastName;

    /**
     * reg number ,can be anything
     */
    private int reg;

    /**
     * occupation of the person
     */
    private String occupation;

    /**
     * get the code
     *
     * @return code of the person
     */
    public int getCode() {
        return code;
    }

    /**
     * set the code
     *
     * @param code to be passed
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * get the first name of person
     *
     * @return first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * set the first name of the user
     *
     * @param firstName passed the name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * get the last name of the person
     *
     * @return the last name of person
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * set the name of the person
     *
     * @param lastName to set the last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * to get the reg number
     *
     * @return the reg number
     */
    public int getReg() {
        return reg;
    }

    /**
     * set the reg number
     *
     * @param reg passed the reg number
     */
    public void setReg(int reg) {
        this.reg = reg;
    }

    /**
     * get the occupation of person
     *
     * @return the occupation of person
     */
    public String getOccupation() {
        return occupation;
    }

    /**
     * set the occupation of person
     *
     * @param occupation
     */
    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }
}

package uml.annotation;


public interface Stereotype {

    public static final Stereotype CONSTRUCTOR = () -> "constructor";

    public String getName();

}

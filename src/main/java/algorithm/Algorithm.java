package algorithm;

@FunctionalInterface
public interface Algorithm<RESULT> {
    RESULT compute();
}

package jet;

import jet.typeinfo.TypeInfo;

public final class IntRange implements Range<Integer>, Iterable<Integer>, JetObject {
    private final static TypeInfo typeInfo = TypeInfo.getTypeInfo(IntRange.class, false);

    private final int startValue;
    private final int excludedEndValue;

    public IntRange(int startValue, int endValue) {
        this.startValue = startValue;
        this.excludedEndValue = endValue;
    }

    @Override
    public boolean contains(Integer item) {
        if (item == null) return false;
        if (startValue < excludedEndValue) {
            return item >= startValue && item < excludedEndValue;
        }
        return item <= startValue && item > excludedEndValue;
    }

    public int getStart() {
        return startValue;
    }

    public int getEnd() {
        return excludedEndValue;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new MyIterator(startValue, excludedEndValue);
    }

    @Override
    public TypeInfo<?> getTypeInfo() {
        return typeInfo;
    }
    
    public static IntRange count(int length) {
        return new IntRange(0, length);
    }

    private static class MyIterator implements Iterator<Integer> {
        private final static TypeInfo typeInfo = TypeInfo.getTypeInfo(MyIterator.class, false);
        private final int lastValue;

        private int cur;
        private boolean reversed;

        public MyIterator(int startValue, int endValue) {
            reversed = endValue <= startValue;
            this.lastValue = reversed ? startValue : endValue-1;
            cur = reversed ? endValue-1 : startValue;
        }

        @Override
        public boolean hasNext() {
            return reversed ? cur >= lastValue : cur <= lastValue;
        }

        @Override
        public Integer next() {
            return reversed ? cur-- : cur++;
        }

        @Override
        public TypeInfo<?> getTypeInfo() {
            return typeInfo;
        }
    }
}

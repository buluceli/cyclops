package com.aol.cyclops.internal.comprehensions;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jooq.lambda.tuple.Tuple2;
import org.pcollections.ConsPStack;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;
import org.pcollections.PStack;

import com.aol.cyclops.internal.comprehensions.comprehenders.Comprehenders;
import com.aol.cyclops.internal.comprehensions.comprehenders.InvokeDynamicComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.MaterializedList;
import com.aol.cyclops.internal.comprehensions.converters.MonadicConverters;
import com.aol.cyclops.types.extensability.Comprehender;

import lombok.AllArgsConstructor;
import lombok.Getter;

public interface ComprehensionsModule {

    static final class BaseComprehensionData {

        private final ContextualExecutor<Object> delegate;
        private ContextualExecutor<Object> currentContext;

        public BaseComprehensionData(final ExecutionState state) {

            delegate = state.contextualExecutor;

        }

        public BaseComprehensionData guardInternal(final Supplier<Boolean> s) {
            ((Foreach) delegate.getContext()).addExpansion(new Filter(
                                                                      "guard", new ContextualExecutor(
                                                                                                      delegate.getContext()) {
                                                                          @Override
                                                                          public Object execute() {
                                                                              currentContext = this;
                                                                              return s.get();
                                                                          }
                                                                      }));
            return this;
        }

        public <R> R yieldInternal(final Supplier s) {
            return (R) ((Foreach) delegate.getContext()).yield(new ExecutionState(
                                                                                  new ContextualExecutor(
                                                                                                         delegate.getContext()) {
                                                                                      @Override
                                                                                      public Object execute() {
                                                                                          currentContext = this;
                                                                                          return s.get();
                                                                                      }
                                                                                  }));

        }

        public <T> T $Internal(final String property) {
            final Object delegate = currentContext.getContext();

            return (T) ((Map) delegate).get(property);

        }

        public BaseComprehensionData $Internal(final String name, final Object f) {

            final Expansion g = new Expansion(
                                              name, new ContextualExecutor(
                                                                           this) {
                                                  @Override
                                                  public Object execute() {
                                                      currentContext = this;
                                                      if (f instanceof InternalSupplier) {
                                                          return ((InternalSupplier) f).get();
                                                      }
                                                      return f;
                                                  }

                                              });

            ((Foreach) delegate.getContext()).addExpansion(g);

            return this;
        }

    }

    /**
     * Class that collects data for free form for comprehensions
     * 
     * @author johnmcclean
     *
     * @param <T>
     *            Variable type
     * @param <R>
     *            Return type
     */
    public static class ComprehensionData<T, R> {
        private final BaseComprehensionData data;

        ComprehensionData(final ExecutionState state) {

            data = new BaseComprehensionData(
                                             state);

        }

        /**
         * Add a guard to the for comprehension
         * 
         * <pre>
         * {@code
         *  	foreachX(c -> c.$("hello",list)
        					   .filter(()->c.<Integer>$("hello")<10)
        						.yield(()-> c.<Integer>$("hello")+2));
        	  }
         * </pre>
         * 
         * @param s
         *            Supplier that returns true for elements that should stay
         *            in the comprehension
         * @return this
         */
        public ComprehensionData<T, R> filter(final Supplier<Boolean> s) {
            data.guardInternal(s);
            return this;

        }

        /**
         * Define the yeild section of a for comprehension and kick of
         * processing for a comprehension
         * 
         * <pre>
         * {@code
         *  	foreachX(c -> c.$("hello",list)
        					   .filter(()->c.<Integer>$("hello")<10)
        						.yield(()-> c.<Integer>$("hello")+2));
        	  }
         * </pre>
         * 
         * @param s
         *            Yield section
         * @return result of for comprehension
         */
        public R yield(final Supplier<R> s) {
            return data.yieldInternal(s);

        }

        /**
         * Extract a bound variable
         * 
         * <pre>
         * {@code
         *  	foreachX(c -> c.$("hello",list)
        					   .filter(()->c.<Integer>$("hello")<10)
        						.yield(()-> c.<Integer>$("hello")+2));
        	  }
         * </pre>
         * 
         * 
         * @param name
         *            Variable name
         * @return variable value
         */
        public T $(final String name) {
            return data.$Internal(name);

        }

        /**
         * Bind a variable in this for comprehension
         * 
         * <pre>
         * {@code
         *  	foreachX(c -> c.$("hello",list)
        					   .filter(()->c.<Integer>$("hello")<10)
        						.yield(()-> c.<Integer>$("hello")+2));
        	  }
         * </pre>
         * 
         * @param name
         *            of variable to bind
         * @param f
         *            value
         * @return this
         */
        public ComprehensionData<T, R> $(final String name, final Object f) {
            data.$Internal(name, f);

            return this;
        }

        /**
         * Lazily bind a variable in this for comprehension
         * 
         * <pre>
         * {@code
         *  	foreachX(c -> c.$("hello",list)
        					   .filter(()->c.<Integer>$("hello")<10)
        						.yield(()-> c.<Integer>$("hello")+2));
        	  }
         * </pre>
         * 
         * @param name
         *            name of variable to bind
         * @param f
         *            value
         * @return this
         */
        public ComprehensionData<T, R> $(final String name, final InternalSupplier f) {
            data.$Internal(name, f);

            return this;
        }

    }

    static interface InternalSupplier extends Supplier<Object> {

    }

    @AllArgsConstructor
    @Getter
    abstract static class ContextualExecutor<T> {
        //Xor<BaseComprehensonData,ForEach>
        private volatile Object context;

        public T executeAndSetContext(final Object context) {
            this.context = context;
            return execute();

        }

        public abstract T execute();
    }

    @Getter
    @AllArgsConstructor
    static class Expansion {

        private final String name;
        private final ContextualExecutor<Object> function;

    }

    @AllArgsConstructor
    final static class ExecutionState<T> {

        public final ContextualExecutor<T> contextualExecutor;

    }

    static class Filter extends Expansion {

        public Filter(final String name, final ContextualExecutor<Object> func) {
            super(name, func);
        }

    }

    @AllArgsConstructor
    static class Yield<T> {

        private final List<Expansion> expansions;
        private final MonadicConverters converters = new MonadicConverters();

        @SuppressWarnings({ "unchecked", "rawtypes" })
        T process(final ContextualExecutor yieldExecutor, final PMap<String, Object> context, final Object currentExpansionUnwrapped,
                final String lastExpansionName, final int index) {

            final Tuple2<Comprehender, Object> comprehender = selectComprehender(currentExpansionUnwrapped).orElseGet(() -> selectComprehender(converters.convertToMonadicForm(currentExpansionUnwrapped)).orElse(new Tuple2(
                                                                                                                                                                                                                             new InvokeDynamicComprehender(
                                                                                                                                                                                                                                                           Optional.ofNullable(currentExpansionUnwrapped)
                                                                                                                                                                                                                                                                   .map(Object::getClass)),
                                                                                                                                                                                                                             currentExpansionUnwrapped)));

            if (expansions.size() == index) {

                return (T) comprehender.v1.map(comprehender.v2, it -> yieldExecutor.executeAndSetContext(context.plus(lastExpansionName, it)));

            } else {
                final Expansion head = expansions.get(index);

                if (head instanceof Filter) {

                    final Object s = comprehender.v1.filter(comprehender.v2, it -> (boolean) head.getFunction()
                                                                                                 .executeAndSetContext(context.plus(lastExpansionName,
                                                                                                                                    it)));
                    return process(yieldExecutor, context, s, lastExpansionName, index + 1);
                } else {

                    final T result = (T) comprehender.v1.executeflatMap(comprehender.v2, it -> {
                        final PMap newMap = context.plus(lastExpansionName, it);
                        return process(yieldExecutor, newMap, head.getFunction()
                                                                  .executeAndSetContext(newMap),
                                       head.getName(), index + 1);
                    });
                    return (T) comprehender.v1.executeflatMap(result, a -> takeFirst(comprehender.v1, a));

                }

            }
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private <T> T takeFirst(final Comprehender comp, final Object o) {
            if (o instanceof MaterializedList) {
                if (((List) o).size() == 0)
                    return (T) comp.empty();

                return (T) comp.of(((List) o).get(0));
            }
            return (T) comp.of(o);
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private Optional<Tuple2<Comprehender, Object>> selectComprehender(final Object structure) {
            if (structure == null)
                return Optional.empty();
            return new Comprehenders().getRegisteredComprehenders()
                                      .stream()
                                      .filter(e -> e.getKey()
                                                    .isAssignableFrom(structure.getClass()))
                                      .map(e -> e.getValue())
                                      .map(v -> new Tuple2<Comprehender, Object>(
                                                                                 v, structure))
                                      .findFirst();
        }

    }

    public static class Foreach<T> {

        private PStack<Expansion> generators = ConsPStack.empty();

        public T yield(final ExecutionState<T> state) {
            final Expansion head = generators.get(0);
            return new Yield<T>(
                                generators).process(state.contextualExecutor, HashTreePMap.empty(),
                                                    head.getFunction()
                                                        .executeAndSetContext(HashTreePMap.empty()),
                                                    head.getName(), 1);
        }

        void addExpansion(final Expansion g) {
            generators = generators.plus(generators.size(), g);
        }

        public static <T> T foreach(final Function<ComprehensionData<?, T>, T> fn) {
            final ContextualExecutor<T> comprehension = new ContextualExecutor<T>(
                                                                                  new Foreach<T>()) {

                @Override
                public T execute() {
                    return fn.apply(new ComprehensionData<>(
                                                            new ExecutionState<>(
                                                                                 this)));
                }

            };
            return comprehension.executeAndSetContext(new Foreach<>());
        }

    }

}

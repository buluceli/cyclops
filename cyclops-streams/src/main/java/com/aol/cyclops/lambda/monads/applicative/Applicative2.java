package com.aol.cyclops.lambda.monads.applicative;

import java.util.function.Function;

import com.aol.cyclops.lambda.monads.ConvertableFunctor;
import com.aol.cyclops.lambda.monads.Functor;

import fj.data.Stream;

@FunctionalInterface
public interface Applicative2<T,T2,R, D extends Functor<R>> extends Functor<Function<? super T,Function<? super T2,? extends R>>> {

	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#map(java.util.function.Function)
	 */
	@Override
	default <U> Functor<U> map(
			Function<? super Function<? super T, Function<? super T2, ? extends R>>, ? extends U> fn) {
		return delegate().map(fn);
	}

	//<U extends Functor<Function<? super T,? extends R>> & Convertable<Function<? super T,? extends R>>> U delegate();
	ConvertableFunctor<Function<? super T,Function<? super T2,? extends R>>>  delegate();
	
	default Applicative<T2,R,D> ap(Functor<T> f){
	
		Function<? super T,Function<? super T2,? extends R>> fn = delegate().get();
		
		return ()->(ConvertableFunctor)f.map(t->fn.apply(t));
		
	}
}

package com.aol.cyclops.matcher.builders;

import java.util.function.Consumer;

import com.aol.cyclops.matcher.PatternMatcher;

public class Matching {

	public static final <T,X> MatchingInstance<T,X> streamCase(Consumer<Case> consumer){
		StreamCase cse = new StreamCase(new PatternMatcher());
		consumer.accept(cse);
		return new MatchingInstance(cse);
	}
	public static final<X> MatchingInstance<? extends Object,X> newCase(Consumer<AggregatedCase<X>> consumer){
		AggregatedCase<X> cse = new AggregatedCase(new PatternMatcher());
		consumer.accept(cse);
		return new MatchingInstance<>(cse);
	}
	public static final<T,X> MatchingInstance<T,X> atomisedCase(Consumer<AtomisedCase> consumer){
		AtomisedCase cse = new AtomisedCase(new PatternMatcher());
		consumer.accept(cse);
		return new MatchingInstance<T,X>(cse);
	}

	
	public static final  StreamCase streamCase(){
		StreamCase cse = new StreamCase(new PatternMatcher());
		return cse;
	}
	
	public static final<USER_VALUE> AtomisedCase<USER_VALUE> atomisedCase(){
		AtomisedCase cse = new AtomisedCase(new PatternMatcher());
		return cse;
	}
	public static final<X> AggregatedCase<X> newCase(){
		AggregatedCase<X> cse = new AggregatedCase<>(new PatternMatcher());
		return cse;
	}
	
}

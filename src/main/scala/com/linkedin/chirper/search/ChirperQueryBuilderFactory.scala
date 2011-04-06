package com.linkedin.chirper.search;

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.queryParser.QueryParser
import org.apache.lucene.queryParser.QueryParser.Operator
import org.apache.lucene.util.Version

import com.sensei.search.nodes.impl.DefaultJsonQueryBuilderFactory
import com.linkedin.chirper.DefaultConfigs

class ChirperQueryBuilderFactor extends DefaultJsonQueryBuilderFactory(DefaultConfigs.queryParser){
}

package com.genie.chatbot.wordnet.jwnl;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.genie.chatbot.conversation.components.ConversationUtility;
import com.genie.chatbot.conversation.core.FrameworkConfiguration;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;
import edu.mit.jwi.morph.WordnetStemmer;

/**
 * @author Rajesh Putta
 */
public class JwnlHelper {

	private static final JwnlHelper helper = new JwnlHelper();
	private Dictionary dictionary = null;
	private edu.mit.jwi.Dictionary jwiDictionary = null;

	private JwnlHelper() {
		
		String baseConfigPath=FrameworkConfiguration.getInstance().getConfiguration(FrameworkConfiguration.CONVERSATION_CONFIG);
		String jwnlConfig=FrameworkConfiguration.getInstance().getConfiguration(FrameworkConfiguration.WORDNET_JWNL_CONFIG);
		String wordnetDictionaryPath=FrameworkConfiguration.getInstance().getConfiguration(FrameworkConfiguration.WORDNET_DICTIONARY_PATH);
		
		try {
			JWNL.initialize(ConversationUtility.loadStream(baseConfigPath+jwnlConfig, true));

			this.dictionary = Dictionary.getInstance();
			
			jwiDictionary = new edu.mit.jwi.Dictionary(new File(baseConfigPath+wordnetDictionaryPath));
			jwiDictionary.open();

		} catch (JWNLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static JwnlHelper getInstance() {
		return helper;
	}
	
	public Set<String> findSynonyms(POS posType, String word) {

		Set<String> synonyms = new HashSet<String>();

		synonyms.add(word);
		
		try {

			IndexWord indexWord = dictionary.lookupIndexWord(posType, word);
			
			if(indexWord==null)
			{
				return synonyms;
			}

			Synset[] senses = indexWord.getSenses();

			for (Synset sense : senses) {
				Word synsetWord=sense.getWord(0);
				synonyms.add(synsetWord.getLemma());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return synonyms;
	}

	public Set<String> findPartOf(POS posType, String word) {

		Set<String> partOfSet = new HashSet<String>();

		try {

			IndexWord indexWord = dictionary.lookupIndexWord(posType, word);

			Synset[] senses = indexWord.getSenses();

			for (Synset sense : senses) {

				Pointer[] holos = sense.getPointers(PointerType.PART_HOLONYM);

				for (Pointer holo : holos) {
					Synset synset = (Synset) (holo.getTarget());
					Word synsetWord = synset.getWord(0);
					System.out.println(synsetWord.getLemma());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return partOfSet;
	}
	
	public Set<String> findStems(edu.mit.jwi.item.POS posType, String word) {
		
		Set<String> stemSet=new HashSet<String>();
		stemSet.add(word);
		
		try
		{
			WordnetStemmer stemmer = new WordnetStemmer(jwiDictionary);
			stemSet.addAll(stemmer.findStems(word, posType));
			
			return stemSet;
		}
		catch(Exception e) {
		}
		
		return stemSet;
	}

	public static void main(String[] args) throws JWNLException {
		
		Set<String> result=new JwnlHelper().findStems(edu.mit.jwi.item.POS.VERB, "ate");
		
		System.out.println(result);
	}

}

/*
    Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
 
package com.esri.core.geometry.examples;

import java.util.ArrayList;

import org.json.*;

import com.esri.core.geometry.GeometryException;
import com.esri.core.geometry.JsonGeometryException;
import com.esri.core.geometry.JsonReader;

/**
 * Sample implementation of a JsonReader around JSONObject class
 * To use pass new JsonObjectReader(jsonObject) to a method that accepts JsonReader instance.
 * It'll read geometry from an JSONObject then.
 *
 */

public final class JsonObjectReader implements JsonReader {

	final static class JSONObjectEnumerator {

		private JSONObject m_jsonObject;
		private boolean m_bStarted;
		private int m_currentIndex;
		private String[] m_keys;

		JSONObjectEnumerator(JSONObject jsonObject) {
			m_bStarted = false;
			m_currentIndex = -1;
			m_jsonObject = jsonObject;
		}

		String getCurrentKey() {
			if (!m_bStarted) {
				throw new GeometryException("invalid call");
			}

			if (m_currentIndex == m_jsonObject.length()) {
				throw new GeometryException("invalid call");
			}

			return m_keys[m_currentIndex];
		}

		Object getCurrentObject() {
			if (!m_bStarted) {
				throw new GeometryException("invalid call");
			}

			if (m_currentIndex == m_jsonObject.length()) {
				throw new GeometryException("invalid call");
			}

			return m_jsonObject.opt(m_keys[m_currentIndex]);
		}

		boolean next() {
			if (!m_bStarted) {
				m_currentIndex = 0;
				m_keys = JSONObject.getNames(m_jsonObject);
				m_bStarted = true;
			} else if (m_currentIndex != m_jsonObject.length()) {
				m_currentIndex++;
			}

			return m_currentIndex != m_jsonObject.length();
		}
	}

	final static class JSONArrayEnumerator {

		private JSONArray m_jsonArray;
		private boolean m_bStarted;
		private int m_currentIndex;

		JSONArrayEnumerator(JSONArray jsonArray) {
			m_bStarted = false;
			m_currentIndex = -1;
			m_jsonArray = jsonArray;
		}

		Object getCurrentObject() {
			if (!m_bStarted) {
				throw new GeometryException("invalid call");
			}

			if (m_currentIndex == m_jsonArray.length()) {
				throw new GeometryException("invalid call");
			}

			return m_jsonArray.opt(m_currentIndex);
		}

		boolean next() {
			if (!m_bStarted) {
				m_currentIndex = 0;
				m_bStarted = true;
			} else if (m_currentIndex != m_jsonArray.length()) {
				m_currentIndex++;
			}

			return m_currentIndex != m_jsonArray.length();
		}
	}
	
	private Object m_object;
	private JsonReader.Token m_currentToken;
	private ArrayList<JsonReader.Token> m_parentStack;
	private ArrayList<JSONObjectEnumerator> m_objIters;
	private ArrayList<JSONArrayEnumerator> m_arrIters;

	public JsonObjectReader(Object object) {
		m_object = object;

		boolean bJSONObject = (m_object instanceof JSONObject);
		boolean bJSONArray = (m_object instanceof JSONArray);

		if (!bJSONObject && !bJSONArray) {
			throw new IllegalArgumentException();
		}

		m_parentStack = new ArrayList<JsonReader.Token>(0);
		m_objIters = new ArrayList<JSONObjectEnumerator>(0);
		m_arrIters = new ArrayList<JSONArrayEnumerator>(0);

		m_parentStack.ensureCapacity(4);
		m_objIters.ensureCapacity(4);
		m_arrIters.ensureCapacity(4);

		if (bJSONObject) {
			JSONObjectEnumerator objIter = new JSONObjectEnumerator((JSONObject) m_object);
			m_parentStack.add(JsonReader.Token.START_OBJECT);
			m_objIters.add(objIter);
			m_currentToken = JsonReader.Token.START_OBJECT;
		} else {
			JSONArrayEnumerator arrIter = new JSONArrayEnumerator((JSONArray) m_object);
			m_parentStack.add(JsonReader.Token.START_ARRAY);
			m_arrIters.add(arrIter);
			m_currentToken = JsonReader.Token.START_ARRAY;
		}
	}

	private void setCurrentToken_(Object obj) {
		if (obj instanceof String) {
			m_currentToken = JsonReader.Token.VALUE_STRING;
		} else if (obj instanceof Double || obj instanceof Float) {
			m_currentToken = JsonReader.Token.VALUE_NUMBER_FLOAT;
		} else if (obj instanceof Integer || obj instanceof Long || obj instanceof Short) {
			m_currentToken = JsonReader.Token.VALUE_NUMBER_INT;
		} else if (obj instanceof Boolean) {
			Boolean bObj = (Boolean) obj;
			boolean b = bObj.booleanValue();
			if (b) {
				m_currentToken = JsonReader.Token.VALUE_TRUE;
			} else {
				m_currentToken = JsonReader.Token.VALUE_FALSE;
			}
		} else if (obj instanceof JSONObject) {
			m_currentToken = JsonReader.Token.START_OBJECT;
		} else if (obj instanceof JSONArray) {
			m_currentToken = JsonReader.Token.START_ARRAY;
		} else {
			m_currentToken = JsonReader.Token.VALUE_NULL;
		}
	}

	Object currentObject_() {
		assert (!m_parentStack.isEmpty());

		JsonReader.Token parentType = m_parentStack.get(m_parentStack.size() - 1);

		if (parentType == JsonReader.Token.START_OBJECT) {
			JSONObjectEnumerator objIter = m_objIters.get(m_objIters.size() - 1);
			return objIter.getCurrentObject();
		}

		JSONArrayEnumerator arrIter = m_arrIters.get(m_arrIters.size() - 1);
		return arrIter.getCurrentObject();
	}

	@Override
	public JsonReader.Token nextToken() throws JsonGeometryException {
		if (m_parentStack.isEmpty()) {
			m_currentToken = null;
			return m_currentToken;
		}

		JsonReader.Token parentType = m_parentStack.get(m_parentStack.size() - 1);

		if (parentType == JsonReader.Token.START_OBJECT) {
			JSONObjectEnumerator iterator = m_objIters.get(m_objIters.size() - 1);

			if (m_currentToken == JsonReader.Token.FIELD_NAME) {
				Object nextJSONValue = iterator.getCurrentObject();

				if (nextJSONValue instanceof JSONObject) {
					m_parentStack.add(JsonReader.Token.START_OBJECT);
					m_objIters.add(new JSONObjectEnumerator((JSONObject) nextJSONValue));
					m_currentToken = JsonReader.Token.START_OBJECT;
				} else if (nextJSONValue instanceof JSONArray) {
					m_parentStack.add(JsonReader.Token.START_ARRAY);
					m_arrIters.add(new JSONArrayEnumerator((JSONArray) nextJSONValue));
					m_currentToken = JsonReader.Token.START_ARRAY;
				} else {
					setCurrentToken_(nextJSONValue);
				}
			} else {
				if (iterator.next()) {
					m_currentToken = JsonReader.Token.FIELD_NAME;
				} else {
					m_objIters.remove(m_objIters.size() - 1);
					m_parentStack.remove(m_parentStack.size() - 1);
					m_currentToken = JsonReader.Token.END_OBJECT;
				}
			}
		} else {
			assert (parentType == JsonReader.Token.START_ARRAY);
			JSONArrayEnumerator iterator = m_arrIters.get(m_arrIters.size() - 1);
			if (iterator.next()) {
				Object nextJSONValue = iterator.getCurrentObject();

				if (nextJSONValue instanceof JSONObject) {
					m_parentStack.add(JsonReader.Token.START_OBJECT);
					m_objIters.add(new JSONObjectEnumerator((JSONObject) nextJSONValue));
					m_currentToken = JsonReader.Token.START_OBJECT;
				} else if (nextJSONValue instanceof JSONArray) {
					m_parentStack.add(JsonReader.Token.START_ARRAY);
					m_arrIters.add(new JSONArrayEnumerator((JSONArray) nextJSONValue));
					m_currentToken = JsonReader.Token.START_ARRAY;
				} else {
					setCurrentToken_(nextJSONValue);
				}
			} else {
				m_arrIters.remove(m_arrIters.size() - 1);
				m_parentStack.remove(m_parentStack.size() - 1);
				m_currentToken = JsonReader.Token.END_ARRAY;
			}
		}

		return m_currentToken;
	}

	@Override
	public JsonReader.Token currentToken() throws JsonGeometryException {
		return m_currentToken;
	}

	@Override
	public void skipChildren() throws JsonGeometryException {
		assert (!m_parentStack.isEmpty());

		if (m_currentToken != JsonReader.Token.START_OBJECT && m_currentToken != JsonReader.Token.START_ARRAY) {
			return;
		}

		JsonReader.Token parentType = m_parentStack.get(m_parentStack.size() - 1);

		if (parentType == JsonReader.Token.START_OBJECT) {
			m_objIters.remove(m_objIters.size() - 1);
			m_parentStack.remove(m_parentStack.size() - 1);
			m_currentToken = JsonReader.Token.END_OBJECT;
		} else {
			m_arrIters.remove(m_arrIters.size() - 1);
			m_parentStack.remove(m_parentStack.size() - 1);
			m_currentToken = JsonReader.Token.END_ARRAY;
		}
	}

	@Override
	public String currentString() throws JsonGeometryException {
		if (m_currentToken == JsonReader.Token.FIELD_NAME) {
			return m_objIters.get(m_objIters.size() - 1).getCurrentKey();
		}

		if (m_currentToken != JsonReader.Token.VALUE_STRING) {
			throw new GeometryException("invalid call");
		}

		return ((String) currentObject_()).toString();
	}

	@Override
	public double currentDoubleValue() throws JsonGeometryException {
		if (m_currentToken != JsonReader.Token.VALUE_NUMBER_FLOAT && m_currentToken != JsonReader.Token.VALUE_NUMBER_INT) {
			throw new GeometryException("invalid call");
		}

		return ((Number) currentObject_()).doubleValue();
	}

	@Override
	public int currentIntValue() throws JsonGeometryException {
		if (m_currentToken != JsonReader.Token.VALUE_NUMBER_INT) {
			throw new GeometryException("invalid call");
		}

		return ((Number) currentObject_()).intValue();
	}

	@Override
	public boolean currentBooleanValue() throws JsonGeometryException {
		JsonReader.Token toc = currentToken();
		if (toc == JsonReader.Token.VALUE_TRUE) {
			return true;
		}
		else if (toc == JsonReader.Token.VALUE_FALSE) {
			return false;
		}
		throw new GeometryException("invalid call");
	}
}

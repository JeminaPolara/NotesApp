/*
 Copyright 2014 Federico Iosue (federico.iosue@gmail.com)

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this

file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under

the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF 

ANY KIND, either express or implied. See the License for the specific language governing

permissions and limitations under the License.
 */
package com.note.pixlui.links;

//Class for storing the information about the Link Location

public class Hyperlink {
	public CharSequence textSpan;
	public InternalURLSpan span;
	public int start;
	public int end;
}
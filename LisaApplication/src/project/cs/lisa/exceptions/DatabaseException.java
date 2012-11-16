/**
 * Copyright 2012 Ericsson, Uppsala University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Uppsala University
 *
 * Project CS course, Fall 2012
 *
 * Projekt DV/Project CS, is a course in which the students develop software for
 * distributed systems. The aim of the course is to give insights into how a big
 * project is run (from planning to realization), how to construct a complex
 * distributed system and to give hands-on experience on modern construction
 * principles and programming methods.
 *
 */
package project.cs.lisa.exceptions;

/**
 * A database exception is thrown in case a database
 * method fails.
 * 
 * @author Harold Martinez
 * @author Kim-Anh Tran
 *
 */
public class DatabaseException extends Exception {

	/**
	 * Generic constructor for raising a DatabaseException.
	 */
	public DatabaseException() {
		super();
	}

	/**
	 * Constructor for raising a DatabaseException with a message
	 * and raising another, possible SQLite, exception.
	 * 
	 * @param detailMessage message describing the exception that occurred
	 * @param throwable 	another exception raised by the program
	 */
	public DatabaseException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	/**
	 * Constructor for raising a DatabaseException with a message.
	 * 
	 * @param detailMessage message describing the exception that occurred
	 */
	public DatabaseException(String detailMessage) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Constructor for raising a DatabaseException with a previous raised exception.
	 * @param throwable another exception raised by the program
	 */
	public DatabaseException(Throwable throwable) {
		super(throwable);
		// TODO Auto-generated constructor stub
	}

	

}

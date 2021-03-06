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
 * Class for setting the relevant exceptions associated with out project.
 * @author Thiago Costa Porto
 */
public class FileException extends Exception {
    
    /**
     * Generic constructor for LisaFileException. To be raised when a 'bad' file
     * is given to the program.
     */
    public FileException() {
        super("Please open a proper file");
    }
    
    /**
     * Constructor for throwing exception with a message field.
     * @param message Message received from throwing call
     */
    public FileException(String message) {
        super(message);
    }
    
    /**
     * Throws FileException with cause. 
     * @param message Err message.
     * @param cause   Cause of throwable.
     */
    public FileException(String message, Throwable cause) {
        super(message, cause);
    }
}

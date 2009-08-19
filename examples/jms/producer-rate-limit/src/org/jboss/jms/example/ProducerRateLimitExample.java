/*
   * JBoss, Home of Professional Open Source
   * Copyright 2005-2008, Red Hat Middleware LLC, and individual contributors
   * by the @authors tag. See the copyright.txt in the distribution for a
   * full listing of individual contributors.
   *
   * This is free software; you can redistribute it and/or modify it
   * under the terms of the GNU Lesser General Public License as
   * published by the Free Software Foundation; either version 2.1 of
   * the License, or (at your option) any later version.
   *
   * This software is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty of
   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
   * Lesser General Public License for more details.
   *
   * You should have received a copy of the GNU Lesser General Public
   * License along with this software; if not, write to the Free
   * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
   * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
   */
package org.jboss.jms.example;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

import org.hornetq.common.example.JBMExample;

/**
 * This example demonstrates how a message producer can be limited to produce messages at a maximum rate
 * specified in messages per sec.
 *
 * @author <a href="tim.fox@jboss.com">Tim Fox</a>
 */
public class ProducerRateLimitExample extends JBMExample
{
   public static void main(String[] args)
   {
      new ProducerRateLimitExample().run(args);
   }

   public boolean runExample() throws Exception
   {
      Connection connection = null;
      InitialContext initialContext = null;
      try
      {
         // Step 1. Create an initial context to perform the JNDI lookup.
         initialContext = getContext(0);

         // Step 2. Perfom a lookup on the queue
         Queue queue = (Queue)initialContext.lookup("/queue/exampleQueue");

         // Step 3. Perform a lookup on the Connection Factory
         ConnectionFactory cf = (ConnectionFactory)initialContext.lookup("/ConnectionFactory");

         // Step 4. Create a JMS Connection
         connection = cf.createConnection();

         // Step 5. Create a JMS Session
         Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

         // Step 6. Create a JMS Message Producer
         MessageProducer producer = session.createProducer(queue);

         System.out.println("Will now send as many messages as we can in 10 seconds...");

         // Step 7. Send as many messages as we can in 10 seconds

         final long duration = 10000;

         int i = 0;

         long start = System.currentTimeMillis();

         while (System.currentTimeMillis() - start <= duration)
         {
            TextMessage message = session.createTextMessage("This is text message: " + i++);

            producer.send(message);
         }

         long end = System.currentTimeMillis();

         double rate = 1000 * (double)i / (end - start);

         System.out.println("We sent " + i + " messages in " + (end - start) + " milliseconds");

         System.out.println("Actual send rate was " + rate + " messages per second");

         // Step 8. For good measure we consumer the messages we produced.

         MessageConsumer messageConsumer = session.createConsumer(queue);

         connection.start();

         System.out.println("Now consuming the messages...");

         i = 0;
         while (true)
         {
            TextMessage messageReceived = (TextMessage)messageConsumer.receive(5000);

            if (messageReceived == null)
            {
               break;
            }

            i++;
         }

         System.out.println("Received " + i + " messages");

         return true;
      }
      finally
      {
         // Step 9. Be sure to close our resources!
         if (initialContext != null)
         {
            initialContext.close();
         }
         if (connection != null)
         {
            connection.close();
         }
      }
   }

}

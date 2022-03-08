#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Copyright 2020 Confluent Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#
# This is a simple example of the SerializingProducer using Avro.
#
import argparse
from enum import Enum

from confluent_kafka import SerializingProducer
from confluent_kafka.schema_registry import SchemaRegistryClient
from confluent_kafka.schema_registry.avro import AvroSerializer
from six.moves import input


class AckEvent(Enum):
    NACK = "NACK"
    ACK = "ACK"


class Notification(object):
    """
    User record
    Args:
        event (AckEvent): Event type
        order_id (str): Order id
        order_total_price (int): Order price
        user_balance (int): Remaining user balance
    """

    def __init__(self, event, order_id, order_total_price, user_balance):
        self.event = event
        self.order_id = order_id
        self.order_total_price = order_total_price
        self.user_balance = user_balance


def notification_to_dict(notification, ctx):
    """
    Returns a dict representation of a User instance for serialization.
    Args:
        notification (User): User instance.
        ctx (SerializationContext): Metadata pertaining to the serialization
            operation.
    Returns:
        dict: Dict populated with user attributes to be serialized.
    """
    # User._address must not be serialized; omit from dict
    return dict(
        event=str(notification.event).split(".")[1],
        orderId=notification.order_id,
        orderTotalPrice=notification.order_total_price,
        userBalance=notification.user_balance
    )


class NotificationKey(object):
    """
    User record
    Args:
        address (str): User address
    """

    def __init__(self, address):
        self.address = address


def notification_key_to_dict(key, ctx):
    """
    Returns a dict representation of a User instance for serialization.
    Args:
        key (NotificationKey): User instance.
        ctx (SerializationContext): Metadata pertaining to the serialization
            operation.
    Returns:
        dict: Dict populated with user attributes to be serialized.
    """
    # User._address must not be serialized; omit from dict
    return dict(user_address=key.address)


def delivery_report(err, msg):
    """
    Reports the failure or success of a message delivery.
    Args:
        err (KafkaError): The error that occurred on None on success.
        msg (Message): The message that was produced or failed.
    Note:
        In the delivery report callback the Message.key() and Message.value()
        will be the binary format as encoded by any configured Serializers and
        not the same object that was passed to produce().
        If you wish to pass the original object(s) for key and value to delivery
        report callback we recommend a bound callback or lambda where you pass
        the objects along.
    """
    if err is not None:
        print("Delivery failed for Notification record {}: {}".format(msg.key(), err))
        return
    print('Notification record {} successfully produced to {} [{}] at offset {}'.format(
        msg.key(), msg.topic(), msg.partition(), msg.offset()))


def main(args):
    topic = args.topic

    key_schema_str = ""
    with open("schema/order-acknowledgment-key.avsc") as f:
        key_schema_str = f.read()

    value_schema_str = ""
    with open("schema/order-acknowledgment-value.avsc") as f:
        value_schema_str = f.read()

    schema_registry_conf = {'url': args.schema_registry}
    schema_registry_client = SchemaRegistryClient(schema_registry_conf)

    key_avro_serializer = AvroSerializer(schema_registry_client,
                                         key_schema_str,
                                         notification_key_to_dict)

    avro_serializer = AvroSerializer(schema_registry_client,
                                     value_schema_str,
                                     notification_to_dict)

    producer_conf = {'bootstrap.servers': args.bootstrap_servers,
                     'key.serializer': key_avro_serializer,
                     'value.serializer': avro_serializer}

    producer = SerializingProducer(producer_conf)

    print("Producing notification records to topic {}. ^C to exit.".format(topic))
    while True:
        # Serve on_delivery callbacks from previous calls to produce()
        producer.poll(0.0)
        try:
            user_address = input("Enter email: ")
            event = input("Enter ACK/NACK: ")
            order_id = input("Enter Order ID: ")
            order_total_price = int(input("Enter Order Total Price: "))
            user_balance = int(input("Enter Remaining User Balance: "))
            key = NotificationKey(address=user_address)
            notification = Notification(event=AckEvent.ACK if event == "ACK" else AckEvent.NACK,
                                        order_id=order_id,
                                        order_total_price=order_total_price,
                                        user_balance=user_balance)
            producer.produce(topic=topic, key=key, value=notification,
                             on_delivery=delivery_report)
        except KeyboardInterrupt:
            break
        except ValueError:
            print("Invalid input, discarding record...")
            continue

    print("\nFlushing records...")
    producer.flush()


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="SerializingProducer Example")
    parser.add_argument('-b', dest="bootstrap_servers", required=True,
                        help="Bootstrap broker(s) (host[:port])")
    parser.add_argument('-s', dest="schema_registry", required=True,
                        help="Schema Registry (http(s)://host[:port]")
    parser.add_argument('-t', dest="topic", default="example_serde_avro",
                        help="Topic name")

    main(parser.parse_args())

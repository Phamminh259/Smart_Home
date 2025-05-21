// @ts-nocheck
import Stomp, {Message} from "stompjs";
import SockJS from "sockjs-client";
import React, {useEffect} from "react";
import {Command} from "../../models/Command.ts";
import {CommandType} from "../../models/enums/CommandType.ts";
import {CommandParams} from "../../models/CommandParams.ts";
import {ACMode} from "../../models/enums/ACMode.ts";

export function WebsocketsSimulator() {

    const shouldConnect = React.useRef(true);
    const count = React.useRef(0);
    const deviceIdRef = React.useRef(-1);
    const onMessageReceived = (payload : Message) => {}

    const onSchedulesReceived = (payload : Message) => {}

    const onStatusReceived = (payload : Message) => {}

    const onCapabilitiesReceived = (payload : Message) => {}


     useEffect(() => {
        if(!shouldConnect.current) return;
        shouldConnect.current = false;
        let i = 0;
        const int = setInterval(() => {
            const deviceIdloc = Math.floor(Math.random() * (1002 - 1) + 1)
            connectSocket(deviceIdloc.toString());
            deviceIdRef.current = deviceIdloc;
            count.current += 1;
            console.log(count.current);
            i++;
            if(i == 1000) {
                clearInterval(int);
            }
        }, 500);

    }, []);

    const connectSocket = (deviceId: string) => {
        try {
            const webChatUrl = "http://localhost:8080/realtime";
            const client = Stomp.over(new SockJS(webChatUrl));

            client.connect(
                {},
                () => {
                    console.log(':::::: SOCKET CONNECTED ::::::');
                    //WASHING MACHINE SIMULATION
                    /*client.subscribe('/wm/freshest/' + deviceId, onMessageReceived);
                    client.subscribe("/wm/status/" + deviceId, onStatusReceived);
                    client.send("/app/capabilities/wm", {}, deviceId.toString());
                    console.log("Sent CAPABILITIES - " + new Date());
                    client.subscribe("/ac/schedules/" + deviceId,onSchedulesReceived);
                    client.subscribe("/wm/capabilities/" + deviceId,onCapabilitiesReceived);
                    setInterval(() => {
                        const params : WashingMachineCommandParams = {
                            userId: Math.floor(Math.random() * (1002 - 1) + 1)!,
                            unit: "C",
                            centrifugeSpeed: 1200,
                            temp: 30,
                            mode: "SPIN_ONLY" as WashingMachineMode,
                            from: 0,
                            taskId: 0
                        }

                        const command : Command = {
                            deviceId: +deviceId,
                            commandType: CommandType.ON,
                            commandParams: params
                        }
                        client.send("/app/command/wm", {}, JSON.stringify(command));
                    }, 15000)*/

                    //AC SIMULATION
                    client.subscribe('/ac/freshest/' + deviceId, onMessageReceived);
                    client.subscribe("/ac/status/" + deviceId, onStatusReceived);
                    client.send("/app/capabilities/ac", {}, deviceId.toString());
                    console.log("Sent capabilities request: " + new Date());
                    client.subscribe("/ac/schedules/" + deviceId,onSchedulesReceived);
                    client.subscribe("/ac/capabilities/" + deviceId,onCapabilitiesReceived);
                    setInterval(() => {
                        const params : CommandParams = {
                            userId: Math.floor(Math.random() * (1002 - 1) + 1),
                            unit: "C",
                            target: 25,
                            fanSpeed: 2,
                            health: true,
                            currentTemp: -1,
                            fungus: true,
                            mode: "AUTO" as ACMode,
                            everyDay: false,
                            from: 0,
                            to: 0,
                            taskId: 0
                        }


                        const command : Command = {
                            deviceId: +deviceId,
                            commandType: CommandType.ON,
                            commandParams: params
                        }
                        client.send("/app/command/ac", {}, JSON.stringify(command));

                    }, 15000);
                },
                () => {
                    console.log(':::::: SOCKET TRYING TO RECONNECT ::::::');
                }
            );
        } catch (err) {
            console.log(
                ':::::: ERROR: SOCKET CONNECTION ::::::' + JSON.stringify(err)
            );
        }
    };
    return (
        <></>
    );
}
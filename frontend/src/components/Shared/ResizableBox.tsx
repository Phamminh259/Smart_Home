// @ts-nocheck
import { ResizableBox as ReactResizableBox } from "react-resizable";

import "react-resizable/css/styles.css";


export function ResizableBox({
                     children,
                     width = 600,
                     height = 300,
                     resizable = true,
                     style = {},
                     className = "",
                                     }) {
    return (
        <div style={{ marginLeft: 20 }}>
            <div
                style={{
                    display: "inline-block",
                    width: "auto",
                    background: "white",
                    padding: ".5rem",
                    borderRadius: "0.5rem",
                    border: "1px solid grey",
                    ...style,
                }}
            >
                {resizable ? (
                    <ReactResizableBox width={width} height={height}>
                        <div
                            style={{
                                width: "100%",
                                height: "100%",
                            }}
                            className={className}
                        >
                            {children}
                        </div>
                    </ReactResizableBox>
                ) : (
                    <div
                        style={{
                            width: `${width}px`,
                            height: `${height}px`,
                        }}
                        className={className}
                    >
                        {children}
                    </div>
                )}
            </div>
        </div>
    );
}

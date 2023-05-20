import { Calendar, momentLocalizer } from 'react-big-calendar';
import moment from 'moment';
import { useNavigate } from 'react-router';
import html2canvas from 'html2canvas';
import { useRef, useState, useEffect } from 'react';

import styled from 'styled-components';
import 'react-big-calendar/lib/css/react-big-calendar.css';
import { SIZE, COLOR } from '../../style/theme';
//아이콘
import {
  IoIosArrowBack,
  IoIosArrowForward,
  IoMdAddCircle,
} from 'react-icons/io';
import { MdOutlineCalendarMonth } from 'react-icons/md';
import { AiOutlineClockCircle } from 'react-icons/ai';
import { TbCapture } from 'react-icons/tb';
import axios from 'axios';

// styled-component
const ToolbarButtonsContainer = styled.div`
  display: flex;
  flex-direction: row;

  > button {
    border: none;
    background-color: inherit;
    cursor: pointer;
    :first-of-type > p {
      font-weight: 700;
      /* color: ${COLOR.main_blue}; */
    }
  }
  > p {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    font-size: 16px;
    font-weight: 600;
    padding-top: 5px;
  }
  @media screen and (min-width: ${SIZE.tablet}) {
    padding-top: 20px;
    margin: 0 auto;
    > button:first-of-type {
      > p {
        font-size: 18px;
        font-weight: 700;
      }
    }
    > p {
      display: flex;
      flex-direction: row;
      justify-content: center;
      align-items: center;
      font-size: 26px;
      padding-top: 5px;
      margin: 0 80px 10px;
      > span:first-child {
        margin-right: 12px;
      }
    }
  }

  @media screen and (min-width: ${SIZE.desktop}) {
    > p {
      margin: 0px 40px 10px;
    }
  }
`;

const CalendarInfoContainer = styled.section`
  display: flex;
  flex-direction: column;
  padding-top: 25px;
  > p {
    font-size: 12px;
    display: flex;
    align-items: center;
    :first-of-type {
      margin-bottom: 5px;
    }
    > span {
      margin-left: 10px;
    }
  }

  @media screen and (min-width: ${SIZE.tablet}) {
    width: 100%;
    padding-top: 10px;
    display: flex;
    flex-direction: row;
    justify-content: end;
    align-items: center;
    > p {
      font-size: 16px;
      :first-of-type {
        margin-bottom: 0;
        margin-right: 20px;
      }
    }
  }
`;

const ToolbarContainer = styled.div`
  width: 100%;
  height: 80px;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background-color: ${COLOR.main_gray};

  @media screen and (min-width: ${SIZE.tablet}) {
    background-color: #ffff;
    height: 120px;
    display: flex;
    flex-direction: column;
  }
`;

const CalendarBottomContainer = styled.div`
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin: 20px;
  /* 캡쳐 버튼 */
  .cal-cap {
    height: 44px;
    width: 44px;
    border-radius: 50%;
    border: none;
    padding-top: 5px;
    background-color: ${COLOR.main_blue};
    text-align: center;
    color: #ffff;
    cursor: pointer;
  }

  .cal-add-btn {
    color: ${COLOR.main_blue};
    cursor: pointer;
  }

  @media screen and (min-width: ${SIZE.desktop}) {
    margin: 10px 40px;
  }
`;

const CalendarContainer = styled.div`
  width: 100%;
  background-color: #fff;

  .rbc-calendar {
    height: 650px;
    .rbc-month-view {
      margin-top: 10px;
      border-top: none;
      .rbc-header {
        padding: 10px 0px;
      }
      .rbc-day-bg {
        cursor: pointer;
      }
    }

    .rbc-today {
      background-color: ${COLOR.main_blue};
    }

    .rbc-date-cell {
      text-align: center;
      padding-top: 5px;
    }
  }

  /* 태블릿 버전 */
  @media screen and (min-width: ${SIZE.tablet}) {
    width: 100%;

    .rbc-calendar {
      padding: 0 40px;
      height: 900px;
      .rbc-month-view {
        margin-top: 20px;
        border: 1px solid #ddd;
      }
    }
  }

  @media screen and (min-width: ${SIZE.desktop}) {
    width: 60%;
    margin: 0 auto 40px;
    box-shadow: 0px 0px 4px 3px rgba(0, 0, 0, 0.1);

    .rbc-month-view {
      margin-top: 20px;
    }
  }
`;

const Toolbar = (props) => {
  const { date } = props;
  const [changeYear, setChangeYear] = useState(date.getFullYear());
  const [changeMonth, setChangeMonth] = useState(date.getMonth() + 1);
  useEffect(() => {
    setChangeYear(date.getFullYear());
    setChangeMonth(date.getMonth() + 1);
    console.log(changeYear, changeMonth);
  }, [date, changeMonth]);

  const navigate = (action) => {
    props.onNavigate(action);
  };

  useEffect(() => {
    axios
      .get(
        `${process.env.REACT_APP_API_URL}/schedules?year=${changeYear}&month=${changeMonth}`,
        {
          headers: {
            Authorization: `${localStorage.getItem('accessToken')}`,
          },
        }
      )
      .then((res) => {
        console.log(res);
      })
      .catch((err) => {
        console.log(err);
      });
  }, [date, changeMonth]);

  return (
    <ToolbarContainer>
      <ToolbarButtonsContainer>
        <button type="button" onClick={navigate.bind(null, 'TODAY')}>
          <p>TODAY</p>
        </button>
        <button type="button" onClick={navigate.bind(null, 'PREV')}>
          <IoIosArrowBack size={30} />
        </button>
        <p>
          <span>{`${date.getFullYear()}년`}</span>
          <span>{`${date.getMonth() + 1}월`}</span>
        </p>
        <button type="button" onClick={navigate.bind(null, 'NEXT')}>
          <IoIosArrowForward size={30} />
        </button>
      </ToolbarButtonsContainer>
      <CalendarInfoContainer>
        <p>
          <MdOutlineCalendarMonth size={16} />
          출석률:<span>80%</span>
        </p>
        <p>
          <AiOutlineClockCircle size={16} />총 운동 :<span>20.5시간</span>
        </p>
      </CalendarInfoContainer>
    </ToolbarContainer>
  );
};

// 캘린더
const CalendarComponent = () => {
  const nav = useNavigate();
  const navToDetail = () => {
    console.log('click');
    nav(`/calendar/:calendarid`);
  };
  const navToAdd = () => {
    nav('/calendar/add');
  };
  moment.locale('ko-KR');
  const localizer = momentLocalizer(moment);

  //캡쳐
  const calendarRef = useRef(null);

  // const captureCalendar = () => {
  //   console.log(calendarRef.current);
  //   if (!calendarRef.current) {
  //     console.log('캡쳐 실패');
  //   }

  //   const calendarElement = async (calendarRef) => {
  //     const canvas = await html2canvas(calendarRef);
  //     document.body.appendChild(canvas);
  //     const dataURL = canvas.toDataURL();
  //     const image = new Image();
  //     image.src = dataURL;
  //     document.body.appendChild(image);
  //   };
  //   console.log(calendarElement);
  //   // html2canvas(calendarElement).then((canvas) => {
  //   //   const dataURL = canvas.toDataURL();
  //   //   const image = new Image();
  //   //   image.src = dataURL;
  //   //   document.body.appendChild(image);
  //   // });
  // };
  const captureCalendar = () => {
    if (!calendarRef.current) {
      console.log('캡쳐 실패');
      return;
    }
    // 달력에 event가 없어서 캡쳐가 안되는 걸지도....
    const calendarElement = calendarRef.current;
    const captureAndSave = async () => {
      console.log(calendarElement);
      if (
        !(calendarElement instanceof Node) ||
        !document.body.contains(calendarElement)
      ) {
        console.log('요소가 문서에 첨부되지 않았습니다.');
        return;
      }

      const canvas = await html2canvas(calendarElement);
      document.body.appendChild(canvas);
    };

    captureAndSave();
  };

  return (
    <CalendarContainer>
      <Calendar
        id="calMain"
        ref={calendarRef}
        localizer={localizer}
        views={['month']}
        components={{
          toolbar: Toolbar,
          event: CustomEvent,
        }}
        onSelectSlot={navToDetail}
      />
      <CalendarBottomContainer>
        <button className="cal-cap" onClick={captureCalendar}>
          <TbCapture size={33} />
        </button>
        <IoMdAddCircle className="cal-add-btn" size={50} onClick={navToAdd} />
      </CalendarBottomContainer>
    </CalendarContainer>
  );
};

export default CalendarComponent;
